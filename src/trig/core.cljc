(ns trig.core
  (:require
   #?(:cljs [taoensso.timbre :as timbre :refer-macros [log warn]]
      :clj [taoensso.timbre :as timbre :refer [log warn]])
   [trig.util :refer [sqr sqrt sin asin cos acos tan atan pi rad->deg deg->rad]]))

(defn pt [x {:keys [a b c]}]
  "Find missing side using the Pythagoreon Theorum"
  (if (= x :c)
    (sqrt (+ (sqr a) (sqr b)))
    (sqrt (- (sqr c) (sqr (if (= x :a) b a))))))

(defn other-sides [x m n side-or-angle-kw]
  (let [required-keys (remove #(= % x) [:a :b :c])
        pos-sides (into {} (filter
                            #(-> % last (and pos?))
                            (select-keys m required-keys)))
        num-sides? (= n (count pos-sides))]
    (when num-sides? pos-sides)))

(defn another-side&angle [x m]
  (when-let [other-side (other-sides x m 1 :side)]
    (when-let [angle (->> [:A :B] (select-keys m) first (apply hash-map))]
      (when (> 90 (-> angle vals first) 0) [other-side angle]))))

(defn kv [m]
  [(-> m keys first) (-> m vals first)])

(def sct-side
  ; vector format is [:side-we-want :side-we-have :angle-we-have]
  {[:b :c :B] [sin * :h :o]
   [:a :c :A] [sin * :h :o]
   [:b :c :A] [cos * :h :a]
   [:a :c :B] [cos * :h :a]
   [:a :b :A] [tan * :a :o]
   [:b :a :B] [tan * :a :o]
   [:c :b :A] [cos / :a :h]
   [:c :a :B] [cos / :a :h]
   [:c :b :B] [sin / :o :h]
   [:c :a :A] [sin / :o :h]
   [:b :a :A] [tan / :o :a]
   [:a :b :B] [tan / :o :a]})

(def sct-angle
  ; vector format is [:angle-we-want :side-we-have :side-we-have2]
  {#{:A :a :b} [atan :a :b]
   #{:B :a :b} [atan :b :a]
   #{:A :b :c} [acos :b :c]
   #{:B :a :c} [acos :a :c]
   #{:A :a :c} [asin :a :c]
   #{:B :b :c} [asin :b :c]})

(defn calc-angle [x sides]
  (let [keyset (into #{} (keys sides))
        [trig dividend-key divisor-key] (get sct-angle (conj keyset x))
        dividend (dividend-key sides)
        divisor (divisor-key sides)]
    (rad->deg (trig (/ dividend divisor)))))

(defn calc-side [x side angle]
  (let [[side-key side-val] (kv side)
        [angle-key angle-val] (kv angle)
        [trig op want have] (get sct-side [x side-key angle-key])]
    (op side-val (-> angle-val deg->rad trig))))

(defn solve-right [m x]
  (if-not (contains? #{:a :b :c :A :B} x)
    (warn
     (str
      "The first argument to lib/right represents the angle or the side of a right triangle that you are trying to solve for.\n\n"
      "It must be a keyword whose value is equal to one of the following:\n :a :b :c :A :B"))
    (if (contains? #{:a :b :c} x)

      ; looking for a side
      (if-let [sides (other-sides x m 2 :side)] ; other 2 sides supplied
        (pt x sides) ; use PT
        (when-let [[side angle] (another-side&angle x m)] ; use sohcahtoa
          (calc-side x side angle)))

      ; looking for an angle
      (when (contains? #{:B :A} x)
        (let [angle-key (if (= x :A) :B :A)
              angle (angle-key m)]
          (if (and angle (pos? angle))

            ; we have and existing angle so just use arithmatic
            (- 180 (+ 90 (or (angle-key m))))

            ; trig
            (when-let [sides (other-sides x m 2 :angle)]
              (calc-angle x sides))))))))

(def angle->side {:A :a :B :b :C :c})
(def side->angle {:a :A :b :B :c :C})
(def n3-key-lut {#{:A :B} :C #{:A :C} :B #{:C :B} :A})
(def s3-key-lut {#{:b :a} :c #{:a :c} :b #{:c :b} :a})

(defn adjacent-angle-kv [m opp-angle-key]
  (-> (dissoc m opp-angle-key)
      (select-keys [:A :B :C])
      kv))

(defn side-kv [m]
  (-> (select-keys m [:a :b :c])
      kv))

(defn angle-kv [m]
  (-> (select-keys m [:A :B :C])
      kv))

(defn sines [a1 a2 a3]
  (map (comp sin deg->rad) [a1 a2 a3]))

(defn aas* [s2 a1 a2]
  (let [a3 (- 180 a1 a2)
        [sin1 sin2 sin3] (sines a1 a2 a3)
        s1 (* s2 (/ sin1 sin2))
        s3 (* s2 (/ sin3 sin2))]
    [a3 s1 s3]))

(defn aas [m]
  "two angles and a side not between"
  (let [[s2-k s2-v] (side-kv m)
        n2-k (s2-k side->angle)
        n2-v (n2-k m)
        [n1-k n1-v] (adjacent-angle-kv m n2-k)
        n3-k (get n3-key-lut #{n1-k n2-k})
        [n3-v s1-v s3-v] (aas* s2-v n1-v n2-v)
        s1-k (n1-k angle->side)
        s3-k (n3-k angle->side)]
    {s1-k s1-v s2-k s2-v s3-k s3-v n1-k n1-v n2-k n2-v n3-k n3-v}))

(defn asa* [a1 s3 a2]
  (let [a3 (- 180 a1 a2)
        [sin1 sin2 sin3] (sines a1 a2 a3)
        s1 (* (/ s3 sin3) sin1)
        s2 (* (/ s3 sin3) sin2)]
    [a3 s1 s2]))

(defn asa [m]
  "two angles and a side between"
  (let [[[n1-k n1-v] [n2-k n2-v]] (into [] (select-keys m [:A :B :C]))
        valid-angles? (> 180 (+ n2-v n1-v))]
    (when-not valid-angles?
      (warn
       (str
        "The sum of the 2 supplied angles must not exceed 180.\n"
        "You have given " n1-k " with of value of " n1-v
        " and " n2-k " with of value of " n2-v ".\n"
        "The sum of these angles is " (+ n1-v n2-v) "."
        )))
    (when valid-angles?
      (let [[s3-k s3-v] (side-kv m)
            n3-k (s3-k side->angle)
            [n3-v s1-v s2-v] (asa* n1-v s3-v n2-v)
            s1-k (n1-k angle->side)
            s2-k (n2-k angle->side)]
        {s1-k s1-v s2-k s2-v s3-k s3-v n1-k n1-v n2-k n2-v n3-k n3-v}))))

(defn sas* [s1 a3 s2]
  (let [s3 (sqrt (-
                  (+ (* s1 s1) (* s2 s2))
                  (* 2 s1 s2 (cos (deg->rad a3)))))
        [shorter-side longer-side] (sort [s1 s2])
        sin-smaller (/ (* shorter-side (sin (deg->rad 49))) s3)
        smaller-angle (rad->deg (asin sin-smaller))
        larger-angle (- 180 a3 smaller-angle)]
    [s3 smaller-angle larger-angle]))

(defn sas [m]
  "two sides and an angle between"
  (let [[n3-k n3-v] (angle-kv m)
        [[s1-k s1-v] [s2-k s2-v]] (sort-by last (select-keys m [:a :b :c]))
        n2-k (s2-k side->angle)
        n1-k (s1-k side->angle)
        s3-k (get s3-key-lut #{s1-k s2-k})
        [s3-v n1-v n2-v] (sas* s1-v n3-v s2-v)]
    {s1-k s1-v s2-k s2-v s3-k s3-v n1-k n1-v n2-k n2-v n3-k n3-v}))

(defn ssa-los [s1 n1 n3]
  (/ (* s1 (sin (deg->rad n3))) (sin (deg->rad n1))))

(defn ssa* [s1 s2 n1 given-side-is-longest?]
  (let [alt? (and (<= n1 90) given-side-is-longest?)
        n2* (rad->deg (asin (/ (* s2 (sin (deg->rad n1))) s1)))
        n2 (if alt? (- 180 n2*) n2*)
        n3 (- 180 n1 n2)
        s3 (ssa-los s1 n1 n3)]
    [n2 n3 s3]))

(defn ssa [m]
"two sides and and angle not between"
  (let [[n1-k n1-v] (angle-kv m)
        s1-k (n1-k angle->side)
        s1-v (s1-k m)
        [s2-k s2-v] (-> (dissoc m s1-k)
                        (select-keys [:a :b :c])
                        side-kv)
        given-side-is-longest? (contains? #{s1-k s2-k} (:longest m))
        [n2-v n3-v s3-v] (ssa* s1-v s2-v n1-v given-side-is-longest?)
        s3-k (get s3-key-lut #{s1-k s2-k})
        n2-k (s2-k side->angle)
        n3-k (s3-k side->angle)]
    {s1-k s1-v s2-k s2-v s3-k s3-v n1-k n1-v n2-k n2-v n3-k n3-v}))

(defn sss* [s1 s2 s3]
  (let [f (fn [x y z] (/ (- (+ (sqr x) (sqr y)) (sqr z)) (* 2 x y)))
        cos1 (f s2 s3 s1)
        cos2 (f s3 s1 s2)
        cos3 (f s1 s2 s3)
        [n1 n2 n3] (map (comp rad->deg acos) [cos1 cos2 cos3])]
    [n1 n2 n3]))

(defn sss [m]
  "three sides"
  (let [{s1 :a s2 :b s3 :c} m]
    (merge m (zipmap [:A :B :C] (sss* s1 s2 s3)))))

(def tri-type
  {#{:A :B :b} [aas :AAS]
   #{:B :C :b} [aas :AAS]
   #{:A :C :c} [aas :AAS]
   #{:B :C :c} [aas :AAS]
   #{:A :B :a} [aas :AAS]
   #{:A :C :a} [aas :AAS]

   #{:A :B :c} [asa :ASA]
   #{:B :C :a} [asa :ASA]
   #{:A :C :b} [asa :ASA]

   #{:b :A :c} [sas :SAS]
   #{:b :C :a} [sas :SAS]
   #{:a :B :c} [sas :SAS]

   #{:b :c :B} [ssa :SSA]
   #{:b :c :C} [ssa :SSA]
   #{:b :a :A} [ssa :SSA]
   #{:b :a :B} [ssa :SSA]
   #{:a :c :A} [ssa :SSA]
   #{:a :c :C} [ssa :SSA]

   #{:b :c :a} [sss :SSS]})

(defn solve
  ([m]
   (let [key-set (into #{} (keys (select-keys m [:a :b :c :A :B :C])))
         [f type] (get tri-type key-set)]
     (assoc (f m) :type type)))
  ([m x]
    (x (solve m))))
