(ns trig.core-test
  (:require
    #?(:cljs [cljs.test :refer-macros [is deftest testing]]
       :clj  [clojure.test :refer [is deftest testing]])
   [trig.core :as trig]
   [trig.util :refer [round sqr sqrt sin asin cos acos tan atan pi rad->deg deg->rad]]))

(deftest right
  (testing "find angle A given sides b & c, or find angle B given sides a & c"
    (let [s1 6750
          s2 8100]
      (is (= (trig/solve-right {:b s1 :c s2} :A)
             (trig/solve-right {:a s1 :c s2} :B)
             (rad->deg (acos (/ s1 s2)))))))

  (testing "find angle B given sides b & a, or find angle A given sides a & b"
    (let [s1 300
          s2 400]
      (is (= (trig/solve-right {:b s1 :a s2} :B)
             (trig/solve-right {:a s1 :b s2} :A)
             (rad->deg (atan (/ s1 s2)))))))

  (testing "find angle A given sides a & c, or find angle B given sides b & c"
    (let [s1 7
          s2 10]
      (is (= (trig/solve-right {:b s1 :c s2})
           {:c 10
            :b 7
            :a 7.1414284285428495
            :C 90
            :B 44.4270040008057
            :A 45.5729959991943}))
      (is (= (trig/solve-right {:b s1 :c s2} :B)
             (trig/solve-right {:a s1 :c s2} :A)
             (rad->deg (asin (/ s1 s2)))))))

  (testing "Find angle A given angle B, or find angle B given angle A"
    (let [angle 46]
      (is (= (trig/solve-right {:A angle} :B)
             (trig/solve-right {:B angle} :A)
             44))))

  (testing "find side b given side c & angle B, or find side a given side c & angle A"
    (let [angle 39
          side 30]
      (is (= (trig/solve-right {:B angle :c side} :b)
             (trig/solve-right {:A angle :c side} :a)
             (* side (-> angle deg->rad sin))))))

  (testing "find side b given side c & angle A, or find side a given side c & angle B"
    (let [angle 60
          side 100]
      (is (= (trig/solve-right {:A angle :c side} :b)
             (trig/solve-right {:B angle :c side} :a)
             (* side (-> angle deg->rad cos))))))

  (testing "find side a given side b & angle A, or find side b given side a & angle B"
    (let [angle 53
          side 7]
      (is (= (trig/solve-right {:A angle :b side} :a)
             (trig/solve-right {:B angle :a side} :b)
             (* side (-> angle deg->rad tan))))))

  (testing "find side c given side b & angle A, or find side c given side a & angle B"
    (let [angle 38
          side 7.8]
      (is (= (trig/solve-right {:A angle :b side} :c)
             (trig/solve-right {:B angle :a side} :c)
             (/ side (-> angle deg->rad cos))))))

  (testing "find side c given side b & angle A, or find side c given side a & angle B"
    (let [angle 68
          side 70]
      (is (= (trig/solve-right {:B angle :b side} :c)
             (trig/solve-right {:A angle :a side} :c)
             (/ side (-> angle deg->rad sin))))))

  (testing "find side b given side a & angle A, or find side a given side b & angle B"
    (let [angle 27
          side 8]
      (is (= (trig/solve-right {:A angle :a side} :b)
             (trig/solve-right {:B angle :b side} :a)
             (/ side (-> angle deg->rad tan))))))

  (testing "pythagoreum theroem"
    (let [a 8
          b 12
          c 14.422205101855956]

      (testing "given sides a & b, find c"
        (is (= (trig/solve-right {:a a :b b} :c)
               (trig/solve-right {:b b :a a} :c)
               (sqrt (+ (* a a) (* b b)))
               c)))

      (testing "given sides c & b, find a"
        (is (= (round (trig/solve-right {:c c :b b} :a))
               (round (sqrt (- (* c c) (* b b))))
               a)))

      (testing "given sides a & c, find b"
        (is (= (round (trig/solve-right {:c c :a a} :b))
               (round (sqrt (- (* c c) (* a a))))
               b))))))

(deftest solve
  (testing "SSS"
    (is (= (trig/solve {:b 6 :c 7 :a 8})
           {:b 6
            :c 7
            :a 8
            :A 75.52248781407008
            :B 46.56746344221023
            :C 57.9100487437197
            :type :SSS})))

  (testing "SSA with alt"
    (is (= (trig/solve {:b 8 :c 13 :B 31 :longest :c})
           {:b 8
            :c 13
            :a 6.7647794204343965
            :B 31
            :C 123.18193954206835
            :A 25.81806045793165
            :type :SSA})))

  (testing "SSA"
    (is (= (trig/solve {:b 8 :c 13 :B 31})
           {:b 8
            :c 13
            :a 15.521570397820525
            :B 31
            :C 56.81806045793165
            :A 92.18193954206835
            :type :SSA})))

  (testing "SAS"
    (is (= (trig/solve {:b 5 :A 49 :c 7})
           {:b 5
            :c 7
            :a 5.298666621959197
            :B 45.41169386690557
            :C 85.58830613309442
            :A 49
            :type :SAS})))

  (testing "SAS"
    (is (= (trig/solve {:b 12.6 :C 41 :B 105})
           {:c 8.557948799276186
            :b 12.6
            :a 7.294380574542001
            :C 41
            :B 105
            :A 34
            :type :AAS})))

  (testing "ASA"
    (is (= (trig/solve {:a 18.9 :B 87 :C 42})
           {:b 24.28642641528604
            :c 16.27309294186255
            :a 18.9
            :B 87
            :C 42
            :A 51
            :type :ASA}))))

