(defproject cjbot "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "GNU GENERAL PUBLIC LICENSE"
            :url "www.gnu.org/licenses/gpl-3.0.txt"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [twitter-api "0.7.4"]
                 [org.clojure/data.json "0.2.1"]
                 ; for logging
                 [com.taoensso/timbre "2.4.1"] 
                 ;; for riak
                 [com.novemberain/welle "1.5.0"]
                 ;; for redis
                 [com.taoensso/carmine "2.2.0"]
                 ;; for loading config files
                 [sonian/carica "1.0.3"]
                 ]
  :main cjbot.core)
