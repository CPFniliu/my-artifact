# Lattice diagram

## names

点阵图, 像素点阵图, 文件点阵图, 数据像素点阵图

## summary

将文件中的数据存储到图片中.

## 像素图片引入

一般情况下, 文件以二进制形式保存, 除此之外, 文件还有其它保存方式. 例如将文件转换为Base64编码保存为文本形式, 那么将文件转换为图片点阵方式保存为像素文件也自然是可以的.

点阵系统中, 一个位置, 我们使用白色代表有这个点, 黑色代表没有这个点, 有这个点可以视为1, 没这个点作为0, 那么一个点就可以对应一个bit.

但是我们使用的图片不是黑白图片, 图片颜色多种多样.

> 假如一个像素有2种颜色, 则一个像素可以代表1个bit
> 假如一个像素有4种颜色, 则一个像素可以代表2个bit
> 假如一个像素有16种颜色, 则一个像素可以代表4个bit
> 假如一个像素有256种颜色, 则一个像素可以代表8个bit
> ....
> 假如一个像素有2^n种颜色, 则一个像素可以代表n个bit

我们使用png图片来表示这个点阵, 一个像素表示点阵中的一个点, 一般来讲一个像素可以使用 `256 * 256 * 256`种颜色, 若是再加上透明度, 则再乘以个 256.

假如png图片中, 每个像素有256种颜色, 那么一个像素就可以代表一个字符(8个bit), 假如屏幕分辨率不大, 只有`1366 * 768`, 那么一张图片便能够存储`1366 * 768 = 1,049,088`bit数据(差不多是1M数据, 1M数据是`1024 * 1024 = 1,048,576`bit).

而如果是远程桌面本地滚动截屏的话, 那么就能够存储更多.

## 像素图片设计

### 图片格式

像素图片格式选择`png`图片格式, 因为`.png`是不会失真, 而 `.jpg` 等图片格式会失真.

### 像素框架

外层灰色
之后一个黑色白色背景框
中间是像素内容

### 像素内容

像素内容会包含, 
图片标记: 用于区分图片版本
像素种类长度
像素0-255;
文件头长度


将一个文件转换为像素图片后, 还能够再从像素图片转换为文件, 那么至少需要将文件的**文件名**等文件信息存入像素图片中.

图片中的像素颜色在截屏时可能会出现失真, 例如像素的颜色在经过截屏之后编程了宁外一种颜色(具体会不会, 我没有具体研究过, 只不过我这么认为而已), 因此在像素图片头部将代表颜色的像素依次写入图片可以有效地防止图片的颜色改变

> 假如点阵有4种颜色, 白, 黑, 黄, 绿, 分别代表0, 1, 2, 3. 那么就在头部将这4中颜色写入图片, 读取图片的时候, 先读取这写颜色, 之后所有的像素都按照这几种颜色进行解析成二进制.

### 像素图片有效区域

一个像素图片可以通过矩形截屏, 截屏之后的图片也能够转换为文件, 那么如何确定像素图片的有效内容呢?

类似于二维码有黑白黑的正方形定位区, 可以用来定位和识别二维码, 但是像素图片不需要那么复杂, 有效内容外包裹一圈黑白相间的点阵即可.

## 像素图片生成解析代码流程

### 像素图片生成流程

1. 首先读取要制作为像素图片的文件, 根据文件的名称, 大小, 以及传入的参数计算并确定出像素图片大小, 像素图片的颜色类型, 每个点的宽度, 图片内容边缘宽度, 定位区等信息.
2. 根据上一步计算的信息生成`.png`图片, 添加背景色为灰色, 之后在内容定位区外边缘绘制出一圈黑白相间的点(左上角的点为黑色起始点, 之后向下, 向右两个不同的方向扩展到右下角).
3. 写入像素图片标记, 像素颜色数量, 像素颜色, 一行像素数量, 像素图片头长度.
4. 将文件名称, 文件日期, 文件MD5码等信息封装后写入像素头.
5. 写入像素内容.
6. 生成像素图片.

### 像素图片解析流程

1. 读取图片
2. 找到像素信息有效位置
3. 获取像素信息数据
4. 解析像素信息数据
5. 将Md5值和解析后的像素信息数据做对比
6. 转储文件.

## demo 示例

