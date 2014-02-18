(ns merchant.edn.writer
  (:use merchant.common)
  (:import java.io.Writer
           java.io.StringWriter
           merchant.common.TaggedValue)
  )


(defn- write-sequential [^String begin, print-one, ^String sep, ^String end, sequence, ^Writer w ]
  (do
    (.write w begin)
    (when-let [xs (seq sequence)]
      (loop [[x & xs] xs]
        (print-one x)
        (when xs
          (.write w sep)
          (recur xs))))
    (.write w end)))

(defrecord EdnWriter [^Writer w wm]
  IValueWriter
  (write [this o] (wm this o))
  (write-nil [this o]     (.write w "nil"))
  (write-boolean [this o] (.write w (str o)))
  (write-symbol [this o]  (.write w (str o)))
  (write-keyword [this o] (.write w (str o)) )
  (write-integer [this o] (.write w (str o)))
  (write-float [this o]   (.write w (str o)))
  (write-string [this o]
    (do (.append w \")
        (dotimes [n (count o)]
          (let [c (.charAt o n)
                e (char-escape-string c)]
            (if e (.write w e)
                  (.append w c))))
        (.append w \")))
  (write-character [this o]
    (do (.append w \\)
        (let [n (char-name-string o)]
          (if n (.append w n)
                (.append w o)))))
  (write-list [this o]    (write-sequential "(" #(write this %) " " ")" o w))
  (write-vector [this o]  (write-sequential "[" #(write this %) " " "]" o w))
  (write-set [this o]     (write-sequential "#{" #(write this %) " " "}" o w))
  (write-map [this o]
    (write-sequential
      "{"
      (fn [e]
        (do (write this (key e))
            (.append w \space)
            (write this (val e))))
      ", "
      "}"
      (seq o)
      w))
  (write-tagged [this tag write-fn o]
    (.write w "#")
    (.write w tag)
    (.write w " ")
    (write-fn this o)))




(defn type-dispatch-map [type-map dispatch-map]
  (into {} (map (fn [[k v]] [k (dispatch-map v)]) type-map)))


(defn make-writer-fn [type-map tag-map default-fn]
  (fn [this o]
    (let [c (class o)]
        (if-let [writefn (type-map c)]
       (writefn this o)
       (if-let [tlmap (tag-map c)]
         (write-tagged this (:tag tlmap) (:write-fn tlmap) o)
         (default-fn this o))))))


(defn edn-writer [jwriter opts]
  (let [{:keys [type-map tag-map default-fn]
         :or {default-fn  (fn [this o] (throw (Throwable. "No EDN mapping for type")))}} opts]
    (EdnWriter.
      jwriter
      (make-writer-fn
        (type-dispatch-map (merge default-type-map type-map) default-dispatch-map)
        (merge default-tag-map tag-map)
        default-fn))))


(defn export-edn
  ([x] (export-edn x {}))
  ([x opts]
   (let [sw (java.io.StringWriter.)
         ew (edn-writer sw opts)]
     (write ew x)
     (str sw))))





