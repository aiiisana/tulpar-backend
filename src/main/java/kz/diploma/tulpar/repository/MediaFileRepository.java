package kz.diploma.tulpar.repository;

import kz.diploma.tulpar.domain.entity.MediaFile;
import kz.diploma.tulpar.domain.enums.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    Optional<MediaFile> findByObjectKey(String objectKey);

    Page<MediaFile> findAllByMediaType(MediaType mediaType, Pageable pageable);

    Page<MediaFile> findAllByUploadedBy(String uploadedBy, Pageable pageable);
}
