package com.eleven22exotics.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "car_images")
@Getter @Setter @NoArgsConstructor
public class CarImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "car_id")
    private Car car;
    @Column(nullable = false) private String imageUrl;
    private String altText;
    private int displayOrder;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ImageType imageType = ImageType.GALLERY;
}
