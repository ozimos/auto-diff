(ns auto-diff
  (:require [sicmutils.env
             :as env
             :refer [D cube square sin cos ->TeX ->infix simplify]]
            [sicmutils.differential :as d]
            [nextjournal.clerk :as clerk]
            [aerial.hanami.common :as hc]
            [aerial.hanami.templates :as ht]
            [vega-plot-setup :refer [ptselect slope-rule]]
            [clojure.java.io :as io])
  (:import java.util.concurrent.Future))


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




(defn height [t v_0 g]
  (env/- (env/* t v_0)
         (env// (env/* g (square t))
                2)))


;; you'll notice the SICMUtils library has its own versions of many clojure core functions

;; Many clojure core functions such as + are genericized in the SICMUtils library so that they can operate on a wide variety of types 

;; If you use the original clojure functions they will blow up somewhere along the line

(defn height-core [t v_0 g]
  (- (* t v_0)
     (/ (* g (square t))
        2)))

;; these work
(height 2 20 10)

(height-core 2 20 10)


(height 't 'v_0 'g)


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

(render (height 't 'v_0 'g))


;; or render with clerk notebook

(defn render-clerk [x] (-> x ->TeX clerk/tex))

(render-clerk (height 't 'v_0 'g))

(defn s-render-clerk [x]
  (-> x simplify render-clerk))


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

(def g 9.81)


(defn height-time [t]
  (env/- (env/* t 40)
         (env// (env/* 9.81 (square t)) 2)))


(height-time 1)

(height 1 40 9.81)

;; Let's plot a graph of projectile height against time

(defn round-to-2dp [r]
  (/ (Math/round (* r 100.0)) 100.0))

(def time-vals (mapv round-to-2dp (range 0 8.4 0.2)))

(def height-vals (map (comp round-to-2dp height-time) time-vals))


(def height-plot-data (map (fn [t h] {:time t :height h}) time-vals height-vals))


(def height-plot (hc/xform ht/line-chart
                           :DATA height-plot-data
                           :WIDTH 700
                           :X :time
                           :Y :height))

(clerk/vl height-plot)


;; ### Rate of Change and Derivatives

;; Looking at this graph we might have some questions

;; - How fast is the ball travelling at some point in time

;; - By how much is the height changing at some point in time

;; - What is the acceleration on the ball at some point in time

;; ### Calculating the approximate slope of a curved line function



(def first-height (height-time 2))
(def second-height (height-time 2.1))

(def slope (/ (- second-height first-height) 0.1))


(defn slope-at
  "Estimate the slope of the function f 
   at the point x
  using accuracy delt"
  [f delt x]
  (/ (- (f (+ x delt))
        (f (- x delt)))
     (* 2 delt)))


(slope-at height-time 0.05 1)

(slope-at height-time 0.05 3)

(slope-at height-time 0.05 5)

(slope-at height-time 0.05 7)

;; the slope-at fn tells us how quickly a fn is changing at a specific input value

;; but this method has some shortcomings

;; the slope-at function is an approximation, 

;; cannot give us the actual change because we always add a small delta

;; for every time t there exists a height and also a rate at which the height is changing

;; this means there is a function from time to rate of height change (velocity)

;; what if we could somehow transform our height function to produce the velocity function

;; we could rewrite the slope-at function be a function of time only


(defn approx-diff
  "Generate a function that calculates
   the slope of the function f 
   at the point x
  using accuracy delt"
  [f delt]
  (fn [x]
    (/ (- (f (+ x delt))
          (f (- x delt)))
       (* 2 delt))))

(def approx-velocity (approx-diff height-time 0.05))

;; ### The SICMUtils D Operator

;; The D operator from SICMUtils maps from function to function

;; converts the height-time function into another function 

;; that tells us how fast the height is changing

;; The projectile velocity is the derivative of its height

(def velocity (D height-time))

(env/kind velocity)

(def compare-D-operators (map (juxt identity
                                    #(slope-at height-time 0.05 %)
                                    (approx-diff height-time 0.05)
                                    (D height-time)) [1 3 5 7]))

(clerk/table (concat [["time" "slope-at" "approx-velocity" "velocity"]] compare-D-operators))


;; The derivative of a function y is another function z 

;; that tells us how much the output of function y changes for small changes in the input to y

;; This process of obtaining a derivative from a function is called differentiation



;;  acceleration is the derivative of velocity

(def acceleration (D velocity))

(def velocity-vals (map (comp round-to-2dp velocity) time-vals))

(def acceleration-vals (map (comp round-to-2dp acceleration) time-vals))

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
                    time-vals height-vals velocity-vals acceleration-vals))

;; projectile graph with dynamic slope line
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
                                            :values [(- g)]
                                            :labelColor "lime"
                                            :tickColor "lime"}}}}


                     (hc/xform  slope-rule
                                :DATA plot-data
                                :YSCALE {:domain [(apply min height-vals) (apply max height-vals)]}
                                :X :x1 :Y :y1 :X2 :x2 :Y2 :y2 :YAXIS nil
                                :TOOLTIP [:XTTIP :YTTIP {:field :x2} {:field :y2} {:field :time}
                                          {:field :velocity} {:field :acceleration}])]))


(clerk/vl slope-plot)


;; ### Maximum Height

;; One application of derivatives is to find the maxima or minima of a function,
;; points along the function's curve where the output of the function's derivative = 0


(s-render-clerk (velocity 't))


;; The height curve peaks between 4 and 4.5 

;; At the very peak, the slope of the curve is zero

;; For a small change in time (the input) we have zero change in height-time


(str (render (velocity 't)) " = 0")


;; $$ t = 40 / 9.81$$

;; t solves to 4.077

(def t-max (/ 40 9.81))


(velocity t-max)

;; maximum height
(def h-max (height-time t-max))


;; ### Differentiation

;; The derivative of some function f is a function g taking the same inputs as f and with an output that equals
;; the rate of change of f's output compared to f's inputs

;; For a small change in the function f inputs how much does f's output change

;; Sensitivity of a function value wrt change in its argument

;; How much the function output wobbles if we perturb the function input

;; The process of obtaining the derivative from a function is called differentiation


(clerk/tex "\\begin{align*}
            {\\frac {d(af+bg)}{dx}}=a{\\frac {df}{dx}}+b{\\frac {dg}{dx}} & \\qquad {\\frac {d(x^r)}{dx}}=rx^{r-1} \\quad for \\space any \\space real \\space number \\space r\\neq 0  \\\\
            {\\frac {d}{dx}}h(x)={\\frac {d}{dz}}f(z)|_{z=g(x)}\\cdot {\\frac {d}{dx}}g(x) & \\qquad (f^{g})'=\\left(e^{g\\ln f}\\right)'=f^{g}\\left(f'{g \\over f}+g'\\ln f\\right) \\\\
            {\\frac {d(fg)}{dx}}={\\frac {df}{dx}}g+f{\\frac {dg}{dx}} & \\qquad  \\left({\\frac {f}{g}}\\right)'={\\frac {f'g-g'f}{g^{2}}}\\quad whenever \\space g \\space is \\space nonzero \\\\
            (\\sin x)'=\\cos x={\\frac {e^{ix}+e^{-ix}}{2}} & \\qquad (\\cos x)'=-\\sin x={\\frac {e^{-ix}-e^{ix}}{2i}}  \\\\
            (\\tan x)'=\\sec ^{2}x={1 \\over \\cos ^{2}x}=1+\\tan ^{2}x
             \\end{align*}")

(defn sample-fn [x] (env/+ (cube x) (env/* 2 (square x)) 1))

(clerk/table (map (fn [f] {:fn (render (f 'x))
                           :derivative (render ((D f) 'x))})
                  [identity square cube sin cos env/tan env/log env/exp sample-fn height-time]))



;; ### Automatic differentiation

;; Allows for the computation of derivative of any computer program

;; For example SICMUtils can be taught to differentiate a java Future


(extend-protocol d/IPerturbed
  Future
  (perturbed? [t] (d/perturbed? @t))
  (replace-tag [t old new] (d/replace-tag @t old new))
  (extract-tangent [t tag] (future (d/extract-tangent @t tag))))

(defn f-cubed [t]
  (future (Thread/sleep 1000) (cube t)))

(def diff (D f-cubed))

@(f-cubed 2) ;; => 8

@(diff 3) ;; => 27

@(diff 'x) ;; => (+ (* x x) (* x (+ x x)))

;; other computational approaches include numeric approximation or symbolic differentiation

;; symbolic differentiation is basically the approach taught in high school

;; SICMUtils uses forward mode automatic differentiation

;; every value in the expression to be differentiated is represented as a dual

;; https://github.com/sicmutils/sicmutils/blob/05302b4492d91cf77bec17220466a3e72d17f39d/src/sicmutils/generic.cljc#L272

;; $$[x + εx^{'}]$$

;; where the first value is the original value and the second value with an ε attached is the differential of the first value

;; The D operator then evaluates the expression

;; As the D operator evaluates the expression, derivatives attached to ε with exponent greater than 1 are discarded

;; $$ε^2$$

;; not an entirely accurate analogy but think of ε as a very small number

;; the value for higher exponents of ε become ever smaller

[0.001 (* 0.001 0.001) (* 0.001 0.001 0.001) (* 0.001 0.001 0.001 0.001)]


(defn test-exp [x] (env/+ (env/* 2 (env/cos (square x))) (env/* 2  x)))

(s-render-clerk (test-exp 'x))

(render (test-exp 'x))

(def derived-test (D test-exp))

;; Let's play computer for the expression above

;; Replacing with duals

;; The mechanical transformation of these rules produces differentiation

;; There are reasons for this but we won't go into details

(s-render-clerk (test-exp 'x))

;; Rules for simple exponents of x, trig functions and constants are built in

;; $$([2 + ε0] \times [x + ε1]) + ([2 + ε0] \times [\cos([x^2 + ε2x]) + unknown])$$

;; another internal rule

;; $$f(x + ε) \approx f(x) + εf^{'}(x)$$

;; $$([2x + ε2 + ε0]) + ([2 + ε0] \times [\cos(x^2) + ε2x(-\sin(x^2))])$$

;; $$[2x + ε2] + [2\cos(x^2) - ε4x\sin(x^2) + ε0]$$

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
         (env// (env/* g (square t)) 2)))


(s-render-clerk (projectile 't 'theta_0))

(def projectile-partial ((env/partial 0) projectile))


(render (simplify (projectile-partial 'theta_0 'x)))


(s-render-clerk (projectile-partial 't 'theta_0))

(def time-range (range 0 8 0.2))

(def theta-range (range (/ env/pi 6) (/ env/pi 2) 0.1))


(def projectile-mesh (vec (for [x time-range]
                            (mapv #(projectile x %) theta-range))))

(clerk/plotly {:data [{:z projectile-mesh :x time-range :y theta-range :type "surface"}]})
