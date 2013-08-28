(ns cjbot.redis
  (:gen-class)
  (:require 
   [clojure.data.json :as json]
   [taoensso.carmine :as car :refer (wcar)]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(def redis-conn {:pool {} :spec {}})
(defmacro wcar* [& body] `(car/wcar redis-conn ~@body))

(defn encode-message [msg]
  (json/write-str msg))

(defn decode-message [msg]
  (json/read-json msg))

(defn save-key-hash-value [key hash value]
  (warn "Saving REDIS key =" key)
  (wcar* (car/hsetnx key hash (encode-message value))))

(defn save-key-value-hash [category object id hash value]
  (let [key (format "%s:%s:%d" category object id)]
    (warn "Saving to REDIS key =" key "hash =" hash)
    (wcar* (car/hsetnx key hash (encode-message value)))))