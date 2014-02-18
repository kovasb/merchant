(defproject merchant "0.1.2-SNAPSHOT"
  :description "FIXME: write description"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :jvm-opts ^:replace ["-server"  "-Xmx1G"]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]]
  :plugins [[lein-cljsbuild "1.0.2"]]
  :source-paths ["src/clj" "src/cljs"]
  :profiles {:dev {:plugins [[com.cemerick/austin "0.1.3"]]}}
  :cljsbuild {
               :builds [{
                          :source-paths ["src/cljs"]
                          :compiler {
                                      :output-to "target/cljsbuild-main.js"
                                      :optimizations :whitespace
                                      :pretty-print true}}]})
