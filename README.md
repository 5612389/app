# app

该项目在Android studio上开发了一个骑行app
该app对接了高德地图的api，已经实现了地图显示、显示定位蓝点、已经绘制骑行路线等功能。
app界面有三个按钮，分别是开始骑行、结束骑行、分享骑行路线。
点击开始骑行，会根据当前行驶路线在地图上进行路线的实时绘制，但目前还有点定位偏移以及绘制卡顿等问题，并且可能视图过大会影响路线的可视性，因为该项目可以动态缩放，所以可以手动放大来避免这个问题。
点击结束骑行，会清除当前的绘制路线方便下一次骑行的绘制。
分享骑行路线功能暂时无法实现，点击只能转发固定文本到微信、qq等等。
