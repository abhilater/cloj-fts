(ns cloj-fts.processor
  (:require [clojure.string :as string]))

(defn ^:private create-tokenizer [split-regex]
  (fn [text]
    (string/split text split-regex)))

(defn ^:private create-normalizer [convertor-function]
  (fn [token] (convertor-function token)))

(def ^:private tokenizer-chain (map create-tokenizer [#" +" #"-"]))
(def ^:private normalizer-chain (map create-normalizer [#(.toLowerCase %)]))

(defn tokenize-text
  "Applies the tokenizer chain and normalizer chain on the text to generate tokens to index"
  [text]
  (->>
    (reduce #(mapcat %2 %1) [text] tokenizer-chain)
    (map #((apply comp normalizer-chain) %))))

