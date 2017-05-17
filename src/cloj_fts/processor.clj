(ns cloj-fts.processor
  (:require [clojure.string :as string]))

(def *space-split-regex* #" +")
(def *to-lowercase-regex* #(.toLowerCase %))
(def *hyphen-regex* #"-")

;;; Creates new tokenizer
(defn- create-tokenizer
  "Returns a tokenizer based on specified split regex. eg space chars"
  [split-regex]
  (fn [text]
    (string/split text split-regex)))

;;; Creates new normalizer
(defn- create-normalizer
  "Returns a normalizer based on specified conversion eg toLowerCase"
  [conversion]
  (fn [token]
    (conversion token)))


;; Space tokenizer
(def space-tokenizer (create-tokenizer *space-split-regex*))

;; hyphen tokenizer
(def hyphen-tokenizer (create-tokenizer *hyphen-regex*))

;; Lowercase normalizer
(def lowercase-normalizer (create-normalizer *to-lowercase-regex*))

;; Tokenizer chain
(def tokenizer-chain (list space-tokenizer hyphen-tokenizer))

;; Normalizer chain
(def normalizer-chain (list lowercase-normalizer))


(defn get-tokens
  "Applies the tokenizer chain on the text to generate
  all the tokens to index"
  [text]
  (let [text-list (list text)]
    ;; tokenizer chain process
    (loop [tchain tokenizer-chain input-list text-list]
      (if (empty? tchain)
        input-list
        (recur (rest tchain) (reduce #(concat ((first tchain) %2) %1) [] input-list))
        ))))

(defn apply-normalizations
  "Applies the normalizer chain on the token list to generate the final
  normalized tokens to feed to the index"
  [token-list]
  ;; apply sequence of normalization fns to the input seq
  (map #((apply comp normalizer-chain) %) token-list))