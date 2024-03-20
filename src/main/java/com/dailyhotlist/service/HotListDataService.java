package com.dailyhotlist.service;

import com.dailyhotlist.mapper.HotListDataMapper;
import com.dailyhotlist.model.HotListData;
import com.dailyhotlist.vo.HotListDataVo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class HotListDataService {
    @Resource
    private HotListDataMapper hotListDataMapper;

    public List<HotListDataVo> selectAllHotListData() {
        List<HotListDataVo> hotListDataVoList = new ArrayList<>();
        for (HotListData hotListData : hotListDataMapper.selectAllHotListData())
            hotListDataVoList.add(new HotListDataVo(hotListData.getId(), decrypt(hotListData.getHotListDataId()), decrypt(hotListData.getHotListDataUrl()), decrypt(hotListData.getHotListDataTitle()), decrypt(hotListData.getHotListDataSubTitle()), decrypt(hotListData.getHotListDataHeat()), decrypt(hotListData.getHotListDataImageUrl()), decrypt(hotListData.getHotListName())));
        return hotListDataVoList;
    }

    public HotListDataVo selectOneHotListData(String hotListDataId, String hotListName) {
        HotListData hotListData = hotListDataMapper.selectOneHotListData(encode(hotListDataId), encode(hotListName));
        return hotListData == null ? null : new HotListDataVo(hotListData.getId(), encode(hotListData.getHotListDataId()), encode(hotListData.getHotListDataUrl()), encode(hotListData.getHotListDataTitle()), encode(hotListData.getHotListDataSubTitle()), encode(hotListData.getHotListDataHeat()), encode(hotListData.getHotListDataImageUrl()), encode(hotListData.getHotListName()));
    }

    public boolean addHotListData(HotListData hotListData) {
        hotListData.setHotListDataId(encode(hotListData.getHotListDataId()));
        hotListData.setHotListDataUrl(encode(hotListData.getHotListDataUrl()));
        hotListData.setHotListDataTitle(encode(hotListData.getHotListDataTitle()));
        hotListData.setHotListDataSubTitle(encode(hotListData.getHotListDataSubTitle()));
        try {
            hotListData.setHotListDataHeat(encode(Float.parseFloat(hotListData.getHotListDataHeat()) + "热度"));
        } catch (Exception e) {
            hotListData.setHotListDataHeat(encode(hotListData.getHotListDataHeat()));
        }
        hotListData.setHotListDataImageUrl(encode(hotListData.getHotListDataImageUrl()));
        hotListData.setHotListName(encode(hotListData.getHotListName()));
        return hotListDataMapper.insertHotListData(hotListData) == 1;
    }

    public boolean updateHotListData(HotListData hotListData) {
        hotListData.setHotListDataId(encode(hotListData.getHotListDataId()));
        hotListData.setHotListDataUrl(encode(hotListData.getHotListDataUrl()));
        hotListData.setHotListDataTitle(encode(hotListData.getHotListDataTitle()));
        hotListData.setHotListDataSubTitle(encode(hotListData.getHotListDataSubTitle()));
        hotListData.setHotListDataHeat(encode(hotListData.getHotListDataHeat()));
        hotListData.setHotListDataImageUrl(encode(hotListData.getHotListDataImageUrl()));
        hotListData.setHotListName(encode(hotListData.getHotListName()));
        return hotListDataMapper.updateHotListData(hotListData) == 1;
    }

    public boolean deleteOneHotListData(int id) {
        int count = hotListDataMapper.deleteOneHotListData(id);
        hotListDataMapper.resetAutoIncrement();
        return count == 1;
    }

    public String encode(String origin) {
        return Base64.getEncoder().encodeToString(origin.getBytes());
    }

    public String decrypt(String encode) {
        return new String(Base64.getDecoder().decode(encode));
    }
}
