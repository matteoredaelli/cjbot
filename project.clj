(defproject cjbot "0.1.1-SNAPSHOT"
  :description "CJBOT: a twitter crawler"
  :url "https://github.com/matteoredaelli/cjbot"
  :license {:name "GNU GENERAL PUBLIC LICENSE"
            :url "www.gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 ;; SOURCES
                 [twitter-api "0.7.5"]

                 ;; for JSON
                 [org.clojure/data.json "0.2.4"]
                 ;; TODO? [cheshire "5.3.1"]

                 ; for logging
                 [com.taoensso/timbre "3.0.0"] 
                 ;; for Redis
                 [com.taoensso/carmine "2.6.0"]
                 ;; for MongoDB
                 [com.novemberain/monger "1.7.0"]

                 ;; for loading config files
                 [sonian/carica "1.1.0"]
                 ;; for parsing commandline args
                 [org.clojure/tools.cli "0.3.1"]

                 ]
  :main cjbot.core)
