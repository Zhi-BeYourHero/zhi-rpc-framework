package com.zhi.compress;

import com.zhi.extension.SPI;

/**
 * @Description
 * @Author WenZhiLuo
 * @Date 2021-02-21 10:45
 */
@SPI
public interface Compress {
    byte[] compress(byte[] bytes);
    byte[] decompress(byte[] bytes);
}
