(ns s3-file-upload-sls.handler
    (:gen-class 
        :methods [^:static [handler [java.util.Map com.amazonaws.services.lambda.runtime.Context] java.util.Map]])
    (:require [clojure.java.io :refer [resource]]
              [clojure.edn :as edn]
              [clojure.data.json :as json]
              [clojure.string :refer [last-index-of]])
    (:import (java.util Date UUID)
             (com.amazonaws HttpMethod)
             (com.amazonaws.services.s3 AmazonS3ClientBuilder)
             (com.amazonaws.auth DefaultAWSCredentialsProviderChain)
             (com.amazonaws.services.s3.model GeneratePresignedUrlRequest)))

(def config (-> (or (resource "config.edn") (resource "config.example.edn"))
                slurp
                edn/read-string))

(def s3-client (-> (AmazonS3ClientBuilder/standard)
                   (.withCredentials (DefaultAWSCredentialsProviderChain.))
                   (.withRegion (:region config))
                   .build))

(defn expirery-date []
    (-> (Date.)
        .getTime
        (+ (* 1000 (:url-expirery-time config)))
        Date.))

(defn presigned-url-request [key expires]
    (-> (GeneratePresignedUrlRequest. (:bucket config) key)
        (.withMethod HttpMethod/PUT)
        (.withExpiration expires)))

(defn presigned-url [key]
    (let [expires (expirery-date)]
        {:expires expires
         :url (->> (presigned-url-request key expires)
                   (.generatePresignedUrl s3-client))}))

(defn file-extension [file-name]
    (let [last-dot (last-index-of file-name ".")]
        (if (some? last-dot)
            (subs file-name (inc last-dot))
            "")))

(defn uuid []
    (-> (UUID/randomUUID)
        .toString))
        
(defn -handler [event context]
    (let [file-name (-> (get event "body")
                        (json/read-str :key-fn keyword)
                        :fileName)
          extension (file-extension file-name)
          key (str (uuid) "." extension)
          {expires :expires url :url} (presigned-url key)]
        {"statusCode" 200
         "headers" {"Content-Type" "application/json"}
         "body" (json/write-str {:file-name file-name 
                                 :key key
                                 :expires (.getTime expires)
                                 :url (.toString url)})}))
