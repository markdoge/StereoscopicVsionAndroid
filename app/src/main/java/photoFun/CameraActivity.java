        /*这页是你要加在测距界面代码中的触发事件
        // 执行StereoBM算法

        private StereoBMUtil stereoBMUtil;//先定义算法类

        UI.setOnClickListener((v, event) -> {//点击屏幕的监听
            Bitmap result = stereoBMUtil.compute(leftBitmap, rightBitmap); //result是灰度图，不用管，这里传进去左右目摄像头图片，你写下
            // 获取触摸点的坐标 x, y
            float x = event.getX();
            float y = event.getY();
            float[] dst = new float[2];
            Matrix imageMatrix = imageViewResult.getImageMatrix();
            Matrix inverseMatrix = new Matrix();
            imageMatrix.invert(inverseMatrix);
            inverseMatrix.mapPoints(dst, new float[]{x, y});
            int dstX = (int) dst[0];
            int dstY = (int) dst[1];
            // 获取该点的三维坐标
            double[] c = stereoBMUtil.getCoordinate(dstX, dstY);
            String s = String.format("点(%d, %d) 三维坐标：[%.2f, %.2f, %.2f]", dstX, dstY, c[0], c[1], c[2]);//三维坐标为真实世界的三维坐标，最终要显示的就是c[2]
            Log.d(TAG, s);//这里改成在屏幕上显示c[2]
            return true;
        });
    }

*/