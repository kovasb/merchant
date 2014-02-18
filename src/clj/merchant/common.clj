(ns merchant.common
  (:require [clojure.edn :as edn]
            [clojure.string :as str])
  (:import java.io.Writer))


(defprotocol ITaggedValue
  (-tag [this])
  (-value [this]))

(defprotocol IValueWriter
  (write [this o])
  (write-nil [this o])
  (write-boolean [this o])
  (write-symbol [this o])
  (write-keyword [this o])
  (write-integer [this ^Long o])
  (write-float [this o])
  (write-string [this o])
  (write-character [this o])
  (write-list [this o])
  (write-vector [this o])
  (write-set [this o])
  (write-map [this o])
  (write-tagged [this tag write-fn o]))

(def default-type-map
  {nil :nil
   java.lang.Boolean :boolean
   java.lang.Integer :integer
   java.lang.Long :integer
   java.lang.Float :float
   java.lang.Double :float
   java.lang.Character :character
   java.lang.String :string
   clojure.lang.Keyword :keyword
   clojure.lang.Symbol :symbol
   ;;clojure.lang.ISeq :list
   clojure.lang.LazySeq :list

   clojure.lang.PersistentList :list
   clojure.lang.PersistentList$EmptyList :list
   clojure.lang.PersistentStructMap :map
   clojure.lang.PersistentTreeMap :map
   clojure.lang.PersistentArrayMap :map
   clojure.lang.PersistentHashMap :map
   clojure.lang.PersistentHashSet :set
   clojure.lang.PersistentTreeSet :set
   clojure.lang.PersistentVector :vector
   })

(def default-dispatch-map
  {:nil write-nil
   :boolean write-boolean
   :symbol write-symbol
   :keyword write-keyword
   :integer write-integer
   :float write-float
   :string write-string
   :character write-character
   :list write-list
   :vector write-vector
   :set write-set
   :map write-map
   :tagged write-tagged})


;; hack to workaround design of clojure.instant public api
(defn- inst-trim [s]
  (subs s 7 (- (.length s) 1 )))


(def default-tag-map
  {java.util.Date {:tag "inst" :write-fn #(write %1 (inst-trim (pr-str %2)))}
   java.util.Calendar {:tag "inst" :write-fn #(write %1 (inst-trim (pr-str %2)))}
   java.sql.Timestamp  {:tag "inst" :write-fn #(write %1 (inst-trim (pr-str %2)))}
   java.util.UUID {:tag "uuid" :write-fn #(write %1 (str %2))}})

(defrecord TaggedValue [tag value]
  ITaggedValue
  (-tag [this] tag)
  (-value [this] value))


