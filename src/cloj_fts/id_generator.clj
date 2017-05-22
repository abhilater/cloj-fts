(ns cloj-fts.id_generator)

;(defprotocol IdGenerator
;  "Abstraction for a unique id generator, default impl in-memory"
;  (-next-id [_] "Get next id")
;  (-init [_ init-val] "Initializes the generator with initial value"))
;
;(defn next-id [id-gen]
;  (-next-id id-gen))
;
;(defn init [id-gen init-val]
;  (-init id-gen init-val))

;
;;;; Generates unique auto increment ids in memory
;(deftype InMemoryIdGenerator [uid]
;  IdGenerator
;  (next-id [_] (swap! uid inc))
;  (init [_ init-val] (reset! uid init-val))
;  )
;
;(deftype DbIdGenerator [uid]
;  IdGenerator
;  (next-id [_] (str "Not implemented"))
;  (init [_ init-val] (str "Not implemented, init-val: " init-val))
;  )


;(defn in-mem-gen []
;  (let [store (atom 0)]
;    (reify IdGenerator
;      (next-id [] (swap! store inc))
;      (init [init-val] (reset! store init-val)))))


;; lexical closure

; Following being my client code I need a mechanism to abstract the
; the object creation i.e (atom 0) out of the client code and simply
; supply the init value i.e 0 and using this init value I want the logic
; to initialize the data structure inside somewhere in the scope of
; deftype implementation, but I cannot call any method of the IdGenerator
; protocol without the existence of an implementation

;;; Instances of id generators
;(def in-mem-id-generator (InMemoryIdGenerator. (atom 0)))
;(def db-id-generator (DbIdGenerator. (atom 0)))
;
;(defn id-generator-factory [type init-val]
;  (case type
;    :in-mem (InMemoryIdGenerator. (atom init-val))
;    :db (DbIdGenerator. (atom init-val))))


