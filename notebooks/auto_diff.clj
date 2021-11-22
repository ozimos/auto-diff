(ns auto-diff
  (:require [nextjournal.clerk :as clerk]
            [clojure.java.io :as io]
            [sicmutils.env :as env
             :refer [D cube square sin cos ->TeX ->infix simplify]]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]))

(require '[vega-plot-setup :refer [ptselect slope-rule]])

;; ### Introduction

;; Have you seen a Math or Physics Equation and wanted to work with them using your computer, but thought that you need some special language such as Matlab or Python?

;; This talk will show that it is possible to accomplish this in Clojure using the SICMUtils Library.

;; This talk will show how to do automatic differentiation in Clojure using the SICMUtils Library. 

;; I will explain what a derivative is, show how you can transcribe equations to a Clojure using the SICM and provide some background on auto differentiation.

;; We will also demonstrate a nice workflow for these types of investigations using two other Clojure libraries

;; Clerk - a notebook that connects to your editor. Covered in detail in a later workshops. Visualizing data with Hanami

;; Visit the reClojure workshop site for details https://www.reclojure.org/#workshops

;; Hanami - a charting tool built on Vega. Covered in detail in some of the other workshops.

;; SICP: 'Structure and Interpretation of Computer Programs' by Gerald Sussman and Hal Abelson.

;; SICM is "Structure and Interpretation of Clasical Mechanics" by Gerald Sussman & Jack Wisdom

;; SICMUtils - a clojure(script) implementation of the scmutils Scheme code used in SICM


;; Other Resources and Libraries see the deps.edn for this project

;; ### References

;;   - [Dual Numbers and Automatic Differentiation By Sam Ritchie](https://samritchie.io/dual-numbers-and-automatic-differentiation/)

;; - https://www.youtube.com/watch?v=7PoajCqNKpg

;; - https://www.youtube.com/watch?v=8AxMp0nfN7s

;; - https://www.youtube.com/watch?v=UCEzBNh9ufs&t=3s

;; - https://www.youtube.com/watch?v=GyUSh0AAloA&t=2037s

;; ### Writing Math Equations in Clojure

;; What if we want to find out how high a ball will go if we toss it straight up

;; This is known as projectile motion and a quick search on the internet will produce the equation

;; This equation shows the height of a projectile after given time t

;; $$y = v_0t – \frac{gt^2}{2}$$

;; Math equations can be represented as Clojure functions

;; We can write out the equation using the sicmutils library

(defn height [t v_0 g]
  (env/- (env/* t v_0)
         (env// (env/* g (square t))  2)))


;; you'll notice the SICMUtils library has its own versions of many clojure core functions

;; Many clojure core functions such as + are genericized in the SICMUtils library so that they can operate on a wide variety of types 

;; If you use the original clojure functions they will blow up somewhere along the line

(defn height-core [t v_0 g]
  (- (* t v_0)
     (/ (* g (square t))  2)))

;; these work
(height 0.2 20 10)

(height-core 0.2  20 10)


;; we can't pass in clojure symbols. this will blow up

  ;; (height-core 't 'v_0 'g)

;; ### How to view your Clojure functions as Math Equations

;; with SICMUtils you can pass in clojure symbols as function arguments

;; and get back symbolic representation of your equation

;; the symbols are quoted to prevent evaluation. 

;; SICMUtils will interpret the symbols as type literal number

(height 't 'v_0 'g)


;; pretty math notation

;; render with sicmutils only
(defn render [x] (-> x simplify ->infix))

;; or render with clerk notebook

(defn render-clerk [x] (-> x ->TeX clerk/tex))

(defn s-render-clerk [x]
  (-> x simplify render-clerk))


;; let's try out our render functions
(render (square (sin (env/+ 'a 3))))
(render-clerk (square (sin (env/+ 'a 3))))


(render-clerk (env/+ (square (sin 'x))
                     (square (cos 'x))))


;; we can get the math notation for any function by calling the function
;; with a quoted symbol (sicmutils generic operations interprets clojure symbols as literal numbers)

(def height-sym (height 't 'v_0 'g))


(render height-sym)

(render-clerk height-sym)

;; ### Recap - Clojure Functions, Math Equations

;; What is a function 

;; A mapping between some input value or a set of input values and an output value

;; A mapping of input to output

;; Maths equations are like clojure functions

;; A projectile is a missile propelled by the exertion of a force which is allowed to move free under the influence of gravity and air resistance - Wikipedia

;; $$y = v_0t  –  \frac{gt^2}{2}$$

;; is just a function that takes as input

;; time t, initial velocity v_0, acceleration due to gravity g

;; and produces the height of projectile to the y

;; ### Graph Plotting

;; let's simplify our projectile height function by using fixed values for all the inputs except time

;; now a function of one argument

;; we'll set acceleration due to gravity to 9.81

(def gr 9.81)

(defn height_time [t]
  (env/- (env/* t 40) (env// (env/* gr (square t)) 2)))


(height_time 0.1)

;; Let's plot a graph of projectile height against time

(defn round-to-2dp [r]
  (/ (Math/round (* r 100.0)) 100.0))

(def time-values (mapv round-to-2dp (range 0 8.4 0.2)))

(def height-vals (map (comp round-to-2dp height_time) time-values))

;; ### The SICMUtils D Operator

;; The D operator from SICMUtils maps from function to function

;; converts the height_time function into another function 

;; that tells us how fast the height is changing

;; we will revisit what the D operator does in detail later

;; The projectile velocity is the derivative of its height

(def velocity (D height_time))

;; similarly the acceleration is the derivative of velocity

(def acceleration (D velocity))

(def velocity-vals (map (comp round-to-2dp velocity) time-values))

(def acceleration-vals (map (comp round-to-2dp acceleration) time-values))

(defn line 
  "Given the x-axis value x, slope s and y-axis intercept b
   return y-axis value"
  [x s b] (round-to-2dp (+ (* s x) b)))

(def plot-data (map (fn [x y z a]
                      (let [dd 0.5
                            x1 (round-to-2dp (- x dd))
                            x2 (round-to-2dp (+ x dd))
                            b  (- y (* z x))
                            y1  (line x1 z b)
                            y2 (line x2 z b)]
                        {:time x :x1 x1 :x2 x2 :height y :y1 y1 :y2 y2 :velocity z :acceleration a})) 
                    time-values height-vals velocity-vals acceleration-vals))

;; projectile graph with dynamic slope
(hc/xform ptselect)
(def slope-plot
  (hc/xform  (assoc ht/layer-chart
                    :encoding {:x {:field :time :type "quantitative"}})
             :DATA plot-data
             :pname> "slope"
             :WIDTH 600 :HEIGHT 600
             :XTTIP {:field :time, :type "quantitative"}
             :YTTIP {:field :height, :type "quantitative"}
             :TOOLTIP [:XTTIP :YTTIP {:field :x}
                       {:field :velocity} {:field :acceleration}]
             :RESOLVE {:scale {:y "independent"}}
             :LAYER [{:encoding {:y {:field :height
                                     :type "quantitative"
                                     :axis {:titleColor "#85A9C5"}}}

                      :layer [{:mark {:type "line"}}
                              ptselect]}
                     {:mark {:type "line" :color "magenta"}
                      :encoding {:y {:field :velocity
                                     :type "quantitative"
                                     :axis {:titleColor "magenta"
                                            :labelColor "magenta"
                                            :tickColor "magenta"}}}}
                     {:mark {:type "line" :color "lime"}
                      :encoding {:y {:field :acceleration
                                     :type "quantitative"
                                     :axis {:titleColor "lime"
                                            :titleBaseline "bottom"
                                            :titleY 550
                                            :values [(- gr)]
                                            :labelColor "lime"
                                            :tickColor "lime"}}}}


                     (hc/xform  slope-rule
                                :DATA plot-data
                                :YSCALE {:domain [(apply min height-vals) (apply max height-vals)]}
                                :X :x1 :Y :y1 :X2 :x2 :Y2 :y2 :YAXIS nil
                                :TOOLTIP [:XTTIP :YTTIP {:field :x2} {:field :y2} {:field :time}
                                          {:field :velocity} {:field :acceleration}])]))


(clerk/vl slope-plot)

;; ### Rate of Change and Derivatives

;; Looking at this graph we might have some questions

;; - How fast is the ball travelling at some point in time

;; - By how much is the height changing at some point in time

;; - What is the acceleration on the ball at some point in time

;; ### Calculating the slope of a function

;; let's assign some values to some of our height function arguments


(def first-val (height 2 40 gr))
(def second-val (height 2.1 40 gr))

(def slope (/ (- second-val first-val) 0.1))


(defn slope-at
  "Estimate the slope of the function f at the point x
  using accuracy delt"
  [f delt x & args]
  (/ (- (apply f (cons (+ x delt) args)) (apply f (cons (- x delt) args))) (* 2 delt)))

;; with our initial height function that takes mutiple parameters
(slope-at height 0.05 2 40 gr)

(slope-at height_time 0.05 2)

;; the slope-at fn tells us how quickly a fn is changing at a specific input value

;; but this method has some shortcomings

;; the slope-at function is an approximation, 

;; cannot give us the actual change because we always add a small delta

;; for every time t there exists a height and also a rate at which the height is changing

;; this means there is a function from time to rate of height change (velocity)

;; what if we could somehow transform our height function to produce the velocity function

;; This process of doing this is called differentiation

;; and our transformed function (velocity) is called the derivative of the height function

;; let us try the D operator from SICMUtils

((D height_time) 2)

(env/kind (D height_time))

;; ### Differentiation

;; a derivative of some fn f is another fn g that tells us the following about f

;; For a small change in the function inputs how much does the output change

;; The rate of change of the output compared to the inputs

;; Sensitivity of a function value wrt change in its argument

;; How much the function output wobbles if we perturb the function input

;; The process of transforms a function into its derivative is called differentiation


;; ### Introductory Calculus formulas for Differentiation

(def texrender (comp env/->tex-equation ->infix simplify))

(defn show-f-and-f' [f]
  (str "\\text{The derivative of }"
       (texrender (f 'x))
       "\\text{  is  }"
       (texrender ((D f) 'x))
       "\\text{.}"))

(clerk/tex (show-f-and-f' square)) 
(clerk/tex (show-f-and-f' cube)) 
(clerk/tex (show-f-and-f' (fn [x] (env/+ (cube x) (env/* 2 (square x)) 1))))

;; ### Derivative of Height using the D Operator

;; (def velocity (D height_time))

(velocity 5)


(env/kind velocity)

;; When rendered

(s-render-clerk (velocity 'x))

;; What is the rate at which the height_time is changing (i.e. the velocity) when time is 4

(velocity 4)


;; The velocity at time 4 is 33

;; ### Maximum Height

;; one application of derivatives is to find the maxima or minima of a function,
;; points along the curve where the derivative = 0


;; The D operator provides slope of the graph
((D height_time) 0.2)

((D height_time) 1.5)
((D height_time) 4.5)

;; we can see the graph peaking. At this point the derivative is zero
;; for a small change in time (the input) we have zero change in height_time


(str (render (velocity 't)) " = 0")

;; t solves to 4.077

((D height_time) 4.077)



 ;; What is automatic differentiation

;; Allows for the computation of derivative of any computer program

;; other computational approaches include numeric approximation or symbolic differentiation

;; symbolic differentiation is basically the approach taught in high school

;; SICMUtils uses foward mode automatic differentiation

;; every value in the expression to be differentiated is represented as a dual

;; https://github.com/sicmutils/sicmutils/blob/05302b4492d91cf77bec17220466a3e72d17f39d/src/sicmutils/generic.cljc#L272

;; $$[x + εx^{'}]$$

;; where the first value is the original value and the second value with an ε attached is the differential of the first value

;; The D operator then evaluates the expression

;; As the D operator evaluates the expression, it throw away ε values with exponent greater than 1


(defn test-exp [x] (env/+ (env/* 2 (env/cos (square x))) (env/* 2  x)))

(s-render-clerk (test-exp 'x))

(render (test-exp 'x))

(def derived-test (D test-exp))

;; Let's play computer for the expression above

;; Replacing with duals

;; The mechanical transformation of these rules produces differentiation

;; There are reasons for this but we won't go into details

;; for small numbers at larger powers  the values are so small they don't matter

;; rate of change inside rate of change

[0.01 (* 0.01 0.01) (* 0.01 0.01 0.01) (* 0.01 0.01 0.01 0.01)]

(s-render-clerk (test-exp 'x))

;; Rules for simple exponents of x, trig functions and constants are built in

;; $$([2 + ε^2] \times [x + ε1]) + ([2 + ε^2] \times [\cos([x^2 + ε2x]) + unknown])$$

;; another internal rule

;; $$f(x + ε) \approx f(x) + εf^{'}(x)$$

;; $$([2x + ε2 + ...]) + ([2 + ε^2] \times [\cos(x^2) + ε2x(-\sin(x^2))])$$

;; $$[2x + ε2] + [2\cos(x^2) - ε4x\sin(x^2) + ...]$$

;; $$[(2x + 2\cos(x^2)) + ε(2 - 4x\sin(x^2))]$$

;; keep only the parts with ε

;; $$2 - 4x\sin(x^2)$$

(s-render-clerk (derived-test 'x))

(render (derived-test 'x))


;; dual numbers are bookkeeping devices for derivatives

;; This is just a surface level treatment. More details at https://samritchie.io/dual-numbers-and-automatic-differentiation/

;; Let's revisit our projectile equation and increase the difficulty

;; This time we'll throw the ball at an angle to the horizontal

;; $$y = v_0 \sin(θ_0) t – \frac{gt^2}{2}$$

;; credits Wikipedia

(clerk/html [:img {:src (slurp (io/resource "projectile.txt"))}])


(defn projectile [t theta_0]
  (env/- (env/* t 40 (sin theta_0))
         (env// (env/* gr (square t)) 2)))


(s-render-clerk (projectile 't 'theta_0))

(def projectile-partial ((env/partial 0) projectile))


(render (simplify (projectile-partial 'theta_0 'x)))


(s-render-clerk (projectile-partial 't 'theta_0))

(def time-range (range 0 8 0.2))

(def theta-range (range (/ env/pi 6) (/ env/pi 2) 0.1))


(def projectile-mesh (vec (for [x time-range]
                            (mapv #(projectile x %) theta-range))))

(clerk/plotly {:data [{:z projectile-mesh :x time-range :y theta-range :type "surface"}]})


(defn dome [x y]
  (env/- (square x) (square y)))

(s-render-clerk (dome 'x 'y))

;; applications 

;; flow rate, epidemiology, modeling disease spread, 

;; COVID SIR  (Susceptible, Infected, Recovered) Model

;; economics, 

;; optimization and machine learning


;; Road Map for the SICMUtils

;; Capabilities

;; Upcoming Features and differences from the original
