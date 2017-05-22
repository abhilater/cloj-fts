(defproject cloj-fts "0.1.0-SNAPSHOT"
  :description "Simple full text search engine in Clojure"
  :url "https://github.com/abhilater/cloj-fts"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.6.3"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [com.stuartsierra/component "0.3.2"]
                 [org.clojure/tools.logging "0.2.6"]
                 ]
  :main ^:skip-aot cloj-fts.core
  :target-path "target/%s"
  :plugins [[lein-ring "0.9.7"]
            [lein-auto "0.1.3"]]

  :ring {:handler cloj-fts.handler/app
         :nrepl   {:start? true
                   :port   9998}}
  ;:profiles {:uberjar {:aot :all}}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}}
  )
