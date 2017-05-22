(ns cloj-fts.processor
  (:require [clojure.string :as string]))

(defn- create-tokenizer [split-regex]
  (fn [text]
    (string/split text split-regex)))

(defn- create-normalizer [convertor-function]
  (fn [token] (convertor-function token)))

(def tokenizer-chain (map create-tokenizer [#" +" #"-"]))
(def normalizer-chain (map create-normalizer [#(.toLowerCase %)]))

(defn tokenize-text
  "Applies the tokenizer chain and normalizer chain on the text to generate tokens to index"
  [text]
  (->>
    (reduce #(mapcat %2 %1) [text] tokenizer-chain)
    (map #((apply comp normalizer-chain) %))))

