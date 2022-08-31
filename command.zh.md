# MadParticle命令使用指南

以下是`/madparticle`（或`/mp`）指令的所有参数和部分说明。参数名称可能与游戏中略有不同。

```
/madparticle /mp
//基本信息
targetParticle (Particle) //要模仿的粒子
spriteFrom (MadParticle.SpriteFrom) //贴图选择方式（随机|按时间变化）
lifeTime (int) //持续时间
alwaysRender (InheritableBoolean) //是否忽略最大粒子距离（默认为32格）
amount (int) //单次生成数量
//生成相关
px, py, pz (double) //生成位置
xDiffuse, yDiffuse, zDiffuse (double) //生成位置误差
vx, vy, vz (double) //生成速度
vxDiffuse, vyDiffuse, vzDiffuse (double) //生成速度误差
//运动相关
collision (InheritableBoolean) //是否与方块碰撞
bounceTime (int) //最大碰撞次数
horizontalRelativeCollisionDiffuse,verticalRelativeCollisionBounce (double) //碰撞时水平扩散/垂直反弹系数
friction, afterCollisionFriction (float) //阻力，碰撞后阻力
gravity, afterCollisionGravity (float) //重力，碰撞后重力
interactWithEntity (InheritableBoolean) //是否被玩家带动
horizontalInteractFactor, verticalInteractFactor (double) //水平扰动系数，垂直扰动系数
//显示相关
renderType (renderType) //渲染模式
r, g, b (double > float) //颜色
beginAlpha, endAlpha (float) //初始/结束不透明度
alphaMode (MadParticle.ChangeMode) //不透明度变化模式（线性|指数|正弦）
beginScale, endScale (float) //初始/结束缩放
scaleMode (MadParticle.ChangeMode) //缩放变化模式（线性|指数|正弦）
//附加内容
whoCanSee (entity) //能够看到此粒子的玩家
expireThen (madParticle command)//粒子消失时产生新粒子
```

> 注意：
>
> - 下列给出的参考值没有经过交叉验证，可能并不准确。
> - 当double或float类型（包括坐标）的参数为整数时，请按需补齐后面的`.0`，而不是只填写整数部分。整数在一些情况下会被补充0.5，这是原版特性。
> -  `InheritableBoolean`是对bool的包装，除`TRUE`和`FALSE`外，还可以填写`INHERIT`。具体内容参见下文`expireThen`。
> 

## targetParticle

决定你想要模仿的粒子。MadParticle（以下简称MP）会尝试从你指定的粒子获取贴图并应用。有一些粒子可能不被MP支持，即使输入指令也不会生成粒子。

## spriteFrom

决定MP模仿粒子时选择贴图的方式。`random`指随机选择一张贴图；`age`指粒子的贴图会根据时间而变化，就像`cloud`粒子那样（生物死亡时的白烟。）

## lifeTime, alwaysRender, amount

`lifeTime`决定粒子的持续时间，单位为tick。生成粒子时有随机的10%误差。

`alwaysRender`决定粒子是否无视最大生成距离。MC原版的最大生成距离为32格。在[Extinguish](https://www.curseforge.com/minecraft/mc-mods/extinguish-by-uss_shenzhou)中，此值被改为了64格。

`amount`决定单次执行命令生成的粒子数量。

## x,y,zDiffuse

决定生成粒子时会在多大的范围内生成。

> 假设`x`是100，`xDiffuse`是5，则粒子会在x=95-105的范围内随机生成。

## vx,vy,vzDiffuse

决定生成粒子时的速度范围。请注意其单位为m/tick，因此一般不需要太大的值。

## collision, bounceTime

`collision`决定粒子是否会与方块发生碰撞。`bounceTime`决定粒子碰撞的次数，在超过此次数后不再进行碰撞判定。

## horizontalRelativeCollisionDiffuse, verticalRelativeCollisionBounce

`horizontalRelativeCollisionDiffuse`决定碰撞时水平方向的扩散范围。基准值为1，指最大100%的动能都用于水平方向扩散。

`verticalRelativeCollisionBounce`决定碰撞时垂直方向的扩散范围。基准值为1，指最大以100%的法向速度反弹。

> 请注意，此处的“水平”“垂直”是相对碰撞表面而言的。

## friction, afterCollisionFriction

`friction`决定粒子运动时速度逐渐减小的幅度。

`afterCollisionFriction`决定粒子碰撞之后的新摩擦力数值。如果你不需要在碰撞后改变摩擦力，填入与`friction`相同数值即可。

> 请注意，摩擦力是指数作用于粒子速度的，即每tick有`vx = vx * friction`。作为参考，玩家正常行走时的摩擦力系数是0.6，在冰上时是0.98。

## gravity, afterCollisionGravity

`gravity`决定粒子运动时所受重力大小。

`afterCollisionGravity`决定粒子碰撞之后的新摩擦力数值。如果你不需要在碰撞后改变重力，填入与`gravity`相同数值即可。

> 与摩擦力不同的是，重力是线性作用于粒子的。作为参考，为0.01的重力值就可以展现出缓缓下落的效果，0.02-0.03的重力值则更接近正常下落的效果。

## interactWithEntity, horizontalInteractFactor, verticalInteractFactor

`interactWithEntity`决定粒子是否会在玩家路过时被带着飘起来。

`horizontalInteractFactor`决定被扰动时水平方向能够获得多少速度。基准值为1，指最大能够获得与玩家相同的水平速度。

`verticalInteractFactor`决定被扰动时垂直方向能够获得多少速度。计算时，取玩家垂直方向上的速度、水平方向速度的几何平均数的最大值，再乘以此值。

> 作为参考，[Extinguish](https://www.curseforge.com/minecraft/mc-mods/extinguish-by-uss_shenzhou)的干粉粒子扰动系数分别为0.3和0.12。
>
> 请注意，由于客户端和服务端的数据差异，由本客户端玩家产生的扰动效果往往和其他玩家产生的效果相比有强弱差异。

## renderType

决定粒子的渲染模式。如果你并不熟悉此项，建议你选择自动填入的值。无自动填入值时建议选择`PARTICLE_SHEET_OPAQUE`或`PARTICLE_SHEET_TRANSLUCENT`。

## r, g, b

决定粒子的贴图会被作何种颜色变化。一般情况下范围应当是0-1，但不作强制规定。尚不明确在范围外的颜色变化规律，但不会产生其他错误。

## beginAlpha, endAlpha, alphaMode

`beginAlpha`决定粒子生成时的不透明度。

`endAlpha`决定粒子消失时的不透明度。如果不需要不透明度变化，填入与`beginAlpha`相同值即可。范围均限定在0-1。

`alphaMode`决定粒子的不透明度如何变化。`linear`指线性变化，`index`指指数变化，`sin`指正弦变化。如果不需要不透明度变化，填入`linear`即可。

> 假设`beginAlpha`为1，`endAlpha`为0.1（即一个逐渐变淡的粒子），粒子存活时间为100tick（5秒），则三种变化模式曲线模拟如图：
>
> ![image](https://user-images.githubusercontent.com/57312492/187078262-1a8b4737-b721-4df4-b092-2ca51bd0279d.png)

>
> 注意，为了更好地突出与其他方式的差异，指数变化时的底数规定为10。
>
> 正弦变化使用`sin`函数的`3/5`次方以突出与`linear`的差异，同时为简化运算，使用查表法和线性插值计算。

## beginScale, endScale, scaleMode

`beginScale`决定粒子生成时的缩放值。

`endScale`决定粒子消失时的缩放值。如果不需要粒子大小变化，填入与`beginScale`相同值即可。

`scaleMode`决定粒子的缩放如何变化。三种选项与上文alpha变化模式相同。

> 假设`beginScale`为0.3，`endScale`为4.5（即一个不断变大的粒子），粒子存活时间为100tick（5秒），则三种变化模式曲线模拟如图：
>
> ![image](https://user-images.githubusercontent.com/57312492/187078284-8321bc4f-5250-49bd-b16b-443b840f02c4.png)


> 以下内容都是可选的，而非必填的。

## whoCanSee

> 感谢`@MalayP`的建议。

`whoCanSee`决定粒子数据会被发往哪些玩家。使用实体目标选择器。不填写时默认粒子被发往维度内的所有玩家。

## expireThen

> 感谢`@MalayP`的建议。

`expireThen`决定粒子消失时会变为其他什么粒子。后接一条完整的`madparticle`指令。这意味着这一整条指令可以嵌套式地延伸，一个父粒子可以有一串子粒子。

> - 与第一条`mp`指令生成父粒子稍有不同的是，在子粒子指令中，你可以在一些参数处填写等于号`=`（对于数值参数），或`INHERIT`（对于布尔（被枚举替代）和枚举），这表示子粒子的这个参数将会继承于其父粒子。
> - `=`和通常的`~`、`^`相似，但不同之处在于，`=`拥有简单的最高优先级，其存在将会覆盖这个参数下的所有其他数字。因此，不仅`=`能够表示“此参数继承于父粒子”，`=-1=`、`3=`、`==1==2==3==`也被接受并表示相同的含义。
> - 请不要在最父粒子的参数处使用`=`和`INHERIT`，这可能会导致意外的行为。
> - 子粒子的`amount`参数将会被忽略，即保持为1。同时我们建议您也输入1，而不是其他值（尽管不会有其他问题）。
> - 父粒子的`whoCanSee`参数将会被忽略，以最后一个`whoCanSee`（即最子粒子）为准。
>
> ---
>
> - 以下参数，有`√`代表其可以被继承，可以使用`=`或`INHERIT`；`×`代表其不能被继承，您必须显式地写明值；`〇`代表其值将会被忽略。
>
> ```
> ... expireThen
> //基本信息
> × targetParticle (Particle) //要模仿的粒子
> √ spriteFrom (MadParticle.SpriteFrom) //贴图选择方式（随机|按时间变化）
> √ lifeTime (int) //持续时间
> √ alwaysRender (InheritableBoolean) //是否忽略最大粒子距离（默认为32格）
> 〇 amount (int) //单次生成数量
> //生成相关
> √ px, py, pz (double) //生成位置
> √〇 xDiffuse, yDiffuse, zDiffuse (double) //生成位置误差 <如果选择继承位置则忽略误差>
> √ vx, vy, vz (double) //生成速度
> √〇 vxDiffuse, vyDiffuse, vzDiffuse (double) //生成速度误差 <如果选择继承速度则忽略误差>
> //运动相关
> √ collision (InheritableBoolean) //是否与方块碰撞
> √ bounceTime (int) //最大碰撞次数
> √ horizontalRelativeCollisionDiffuse,verticalRelativeCollisionBounce (double) //碰撞时水平扩散/垂直反弹系数
> × friction, afterCollisionFriction (float) //阻力，碰撞后阻力 <有歧义而不允许继承>
> × gravity, afterCollisionGravity (float) //重力，碰撞后重力 <有歧义而不允许继承>
> √ interactWithEntity (InheritableBoolean) //是否被玩家带动
> √ horizontalInteractFactor, verticalInteractFactor (double) //水平扰动系数，垂直扰动系数
> //显示相关
> × renderType (renderType) //渲染模式
> √ r, g, b (double > float) //颜色
> × beginAlpha, endAlpha (float) //初始/结束不透明度 <有歧义而不允许继承>
> √ alphaMode (MadParticle.ChangeMode) //不透明度变化模式（线性|指数|正弦）
> × beginScale, endScale (float) //初始/结束缩放 <有歧义而不允许继承>
> √ scaleMode (MadParticle.ChangeMode) //缩放变化模式（线性|指数|正弦）
> //附加内容
> 〇 whoCanSee (entity) //能够看到此粒子的玩家
> expireThen ...

