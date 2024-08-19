package Project.Pocket.Image.service;

import Project.Pocket.Image.entity.Image;
import Project.Pocket.Image.entity.ImageDto;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class ImageService {



    public static ImageDto mapToDto(Image image){
        ImageDto dto = new ImageDto();
        dto.setId(image.getId());
        dto.setUrl(image.getUrl());
        return dto;
    }
}
