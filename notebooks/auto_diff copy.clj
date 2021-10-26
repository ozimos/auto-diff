;; exploration of automatic differentiation

(ns auto-diff
  (:require [nextjournal.clerk :as clerk]
            [nextjournal.clerk.viewer :as cv]
            [sicmutils.env :as env
             :refer [D cube square sin cos ->TeX compose ->infix simplify]]
            [tech.viz.vega :as v]))

(clerk/md "### Resources\n
           
  [Tech Viz Vega Library](https://github.com/techascent/tech.viz) \n
  [SICMUtils Library](https://github.com/sicmutils/sicmutils) \n
  [Clerk Notebook](https://nextjournal.com/mk/clerk-preview) \n")

 ;; Show my deps.edn


(clerk/md "### Bibliography\n
           
           [Dual Numbers and Automatic Differentiation By Sam Ritchie](https://samritchie.io/dual-numbers-and-automatic-differentiation/)
           ")

 ;; What is differentiation
 ;; What is automatic differentiation


(def render (comp ->infix simplify))


;; some math notation

(clerk/tex (->TeX (env/+ (square (sin 'x))
                         (square (cos 'x)))))

;; this function shows the distance traveled after given time
(defn distance-fn [x]
  (env/+ (square x) (cube x)))



(render (square (sin (env/+ 'a 3))))


;; the math notation for the function
(clerk/tex (->TeX (env/+ (square 'x) (cube 'x))))

(def values (range 0 0.5 10))



(def ans (map distance-fn values))

(def plot-data (map (fn [a b] {:a a :b b}) values ans))



(cv/vl (v/scatterplot plot-data :a :b))
(render (simplify ((D distance-fn) 'x)))

;; The slope of the graph at 4
((env/D distance-fn) 4)



((env/D distance-fn) 6)






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
