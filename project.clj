(defproject cjbot "0.1.1-SNAPSHOT"
  :description "CJBOT: a twitter crawler"
  :url "https://github.com/matteoredaelli/cjbot"
  :license {:name "GNU GENERAL PUBLIC LICENSE"
            :url "www.gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [twitter-api "0.7.5"]
                 [org.clojure/data.json "0.2.4"]
                 ; for logging
                 [com.taoensso/timbre "3.0.0"] 
                 ;; for riak
                 [com.novemberain/welle "1.5.0"]
                 ;; for redis
                 [com.taoensso/carmine "2.4.6"]
                 ;; for loading config files
                 [sonian/carica "1.0.4"]
                 ;; for parsing commandline args
                 [org.clojure/tools.cli "0.3.1"]
                 ]
  :main cjbot.core)
