(ns cjbot.mongodb
  (:gen-class)
  (:use [carica.core])
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]
           [com.mongodb DB WriteConcern])
  (:require 
   [monger.core :as mg]
   [monger.collection :as mc]
   [monger.operators :refer :all]
   [clojure.data.json :as json]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(let [^MongoOptions opts (mg/mongo-options :threads-allowed-to-block-for-connection-multiplier 300)
      ^ServerAddress sa  (mg/server-address (config :mongodb :host) (config :mongodb :port))]
  (mg/connect! sa opts)
  (mg/set-db! (mg/get-db (config :mongodb :db))))
