# DocParser重复执行结果不正确
1. DocParser第一次执行，各表数量object=60，doc=60，relation=0，没有保存关联关系数据
2. DocParser第二次执行，各表数量object=120，doc=70，relation=0，object多了doc多了部分parentName为空的数据
