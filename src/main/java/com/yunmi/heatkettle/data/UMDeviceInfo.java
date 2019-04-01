package com.yunmi.heatkettle.data;

/**
 * 滤水壶属性
 * Created by William on 2017/5/18.
 */

public class UMDeviceInfo {
    public int water_remain_time;   // 水的留存时间，换水后清 0 (单位：小时)
    public int flush_time;  // 待冲洗时间，冲洗完后开始计算 (单位：小时)
    public int flush_flag;  // 冲洗过程 (0：正常状态；1：阶段一，制冷水10秒；2：阶段二，设定85℃，冲洗20秒；3：阶段三，静置30秒)
    public int tds; // 本次制水入水水质（平均值）
    public int time;    // 当前设备时间
    public int curr_tempe;  // 当前温度
    public int setup_tempe; // 设置温度
    public int custom_tempe1;   // 热水按键，自定义温度
    public int min_set_tempe;   // 最小可加热温度
    public int drink_remind;    // 喝水提醒, 0: 不提醒；1: 提醒
    public int drink_remind_time;   // 喝水提醒时间(单位: 小时)
    public int run_status = 0;
    public int work_mode = 0;   // 0:常温；1:温水；2:鲜开水
    public int drink_time_count = 0;    // 喝水时间计时
    public byte[] statusByte;             // 异常状态
    public int statusCount = 0; // 异常统计

    @Override
    public String toString() {
        return "UMDeviceInfo {" +
                "water_remain_time:" + water_remain_time + "," +
                "flush_time:" + flush_time + "," +
                "flush_flag:" + flush_flag + "," +
                "tds:" + tds + "," +
                "time:" + time + "," +
                "curr_tempe:" + curr_tempe + "," +
                "setup_tempe:" + setup_tempe + "," +
                "custom_tempe1:" + custom_tempe1 + "," +
                "min_set_tempe:" + min_set_tempe + "," +
                "drink_remind:" + drink_remind + "," +
                "drink_remind_time" + drink_remind_time + "," +
                "run_status" + run_status + "," +
                "work_mode" + work_mode + "," +
                "drink_time_count" + drink_time_count;
    }
}
