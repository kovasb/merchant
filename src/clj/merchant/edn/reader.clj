(ns merchant.edn.reader
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import java.io.Writer))


(defn- record-name [record-class]
  "Returns the record's name as a String given the class `record-class`."
  (str/replace (pr-str record-class) \_ \-))


(defn class->factory
  "Returns the map-style record factory for the `record-class`."
  [record-class]
  (let [cname (record-name record-class)
        dot (.lastIndexOf ^String cname ".")]
    (when (pos? dot)
      (resolve (symbol (str (subs cname 0 dot) "/map->" (subs cname (inc dot))))))))

(defn tl-mapping-to-constructor [tl-mapping]
  [(symbol (:tag tl-mapping)) (class->factory (:record tl-mapping))])


(defn import-edn [data-string tl-mappings]
  (let [readers (into {} (map tl-mapping-to-constructor tl-mappings))]
    (edn/read-string {:readers readers} data-string)))




