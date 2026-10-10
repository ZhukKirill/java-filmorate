package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.MpaInfo;

public class MpaMapper {

    public static MpaDto mapToMpaDto(MpaInfo mpaInfo) {
        MpaDto mpaDto = new MpaDto();
        mpaDto.setId(mpaInfo.getId());
        mpaDto.setName(mpaInfo.getName());
        return mpaDto;
    }

    public static MpaInfo mapToMpaInfo(MpaDto mpaDto) {
        MpaInfo mpaInfo = new MpaInfo();
        mpaInfo.setId(mpaDto.getId());
        mpaInfo.setName(mpaDto.getName());
        return mpaInfo;
    }
}
