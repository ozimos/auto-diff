;; exploration of automatic differentiation

(ns auto-diff
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as cv]
            [sicmutils.env :as env
             :refer [D cube square sin cos ->TeX compose ->infix simplify]]
            [tech.viz.vega :as v]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [aerial.hanami.core :as hmi]))

;; Resources
 ;; Show deps.edn

(clerk/md "### Bibliography\n
           
           [Dual Numbers and Automatic Differentiation By Sam Ritchie](https://samritchie.io/dual-numbers-and-automatic-differentiation/)
           ")

 ;; What is differentiation

;; Process of producing a derivative

;; What is a derivative
;; Sensitivity of a function value wrt change in its argument
;; How much the function output wobbles if we perturb the function input
;; What is function 
;; mapping of input to output
 ;; What is automatic differentiation
;; not numeric approximation or symbolic differentiation
;; auto diff is different from symbolic differentiation
;; History of automatic differentiation
;;- What is Differentiation, What are the various forms of Differentiation: Symbolic, Numerical
;; How can we use Differentiation
;; Ideas in the big feature requests







;; we can define functions using the sicmutils library
;; this function shows the distance traveled after given time
(defn distance-fn [x]
  (env/+ (square x) (cube x)))



;; pretty math notation

;; let's define a render function
(def render (comp ->infix simplify))

;; or render with clerk
(def s-render-clerk (comp clerk/tex ->TeX simplify))

(def render-clerk (comp clerk/tex ->TeX))


;; render expressions as text
(render (square (sin (env/+ 'a 3))))

;; or have the clerk notebook render it for us
(render-clerk (env/+ (square (sin 'x))
                     (square (cos 'x))))



;;we can get the math notation for the function by calling the function
;; on a quoted value

(def distance-fn-sym (distance-fn 'x))

(render-clerk distance-fn-sym)

;; The analytical derivative of the distance function

(def derivative-distance-fn ((D distance-fn) 'x))

(s-render-clerk derivative-distance-fn)


;; let's plot a graph of the distance function

(def time-values (map #(/ % 10) (range 100)))

(def distance-vals (map distance-fn time-values))

(def plot-data (map (fn [x y] {:time x :distance y}) time-values distance-vals))



(cv/vl (v/scatterplot plot-data :time :distance))


(def hanami-vega
  (hc/xform ht/line-chart
            :DATA plot-data
            :X :time
            :Y :distance))

(cv/vl hanami-vega)

;; The D operator provides slope of the graph at 4
((D distance-fn) 4)



((D distance-fn) 6)

;; do an example with multiple input arguments

;; applications used in machine learning

;; a technique in machine learning backpropagation is actually a form of reverse mode auto differentiation




(comment
  ((D cube) 5)

  (distance-fn 5)

  ((D distance-fn) 1)

  ((D (env/+ (square sin) (square cos))) 3)


  (D (env/+ (square sin) (square cos)))


  (defn unity1 [x]
    (env/+ (env/square (env/sin x))
           (env/square (env/cos x))))

  ((env/D unity1) 4)

  (def unity2
    (env/+ (compose square sin)
           (compose square cos)))

  (require '[clojure.tools.deps.alpha.repl :as hotload])



  (->TeX unity2)

  (->TeX (env/+ (square (sin 'x))
                (square (cos 'x)))))
