(defproject trig "0.1.1-SNAPSHOT"
  :description "Triangle solver for Clojure(Script)"
  :url "http://github.com/paintparty/trig-cljc"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [com.taoensso/timbre "4.10.0"]]
  :repl-options {:init-ns trig.core})
