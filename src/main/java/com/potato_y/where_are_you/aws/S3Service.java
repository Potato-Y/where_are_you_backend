package com.potato_y.where_are_you.aws;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 s3Client;

  @Value("${aws.s3.bucket-name}")
  private String bucketName;

  @Value("${aws.s3.default-image-path}")
  private String defaultImagePath;

  public String uploadPostFile(MultipartFile image, String path) throws IOException {
    String fileName = this.getFileName(image, path);
    String filePath = defaultImagePath + fileName;
    ObjectMetadata objectMetadata = this.getObjectMetadata(image);

    PutObjectRequest request = new PutObjectRequest(
        bucketName, filePath, image.getInputStream(), objectMetadata
    );

    s3Client.putObject(request);

    return filePath;
  }

  public void deletePostFolder(String folderPath) {
    if (!folderPath.endsWith("/")) {
      folderPath += "/";
    }
    folderPath = defaultImagePath + folderPath;

    ObjectListing objectListing = s3Client.listObjects(bucketName, folderPath); // 목록 가져오기

    // 모든 객체 삭제
    while (true) {
      for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
        s3Client.deleteObject(bucketName, objectSummary.getKey());
      }

      // 다음 페이지가 있으면 계속 진행
      if (objectListing.isTruncated()) {
        objectListing = s3Client.listNextBatchOfObjects(objectListing);
      } else {
        break;
      }
    }
  }

  private String getFileName(MultipartFile image, String imagePath) {
    String originalFilename = image.getOriginalFilename();
    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

    return imagePath + "/" + UUID.randomUUID() + extension;
  }

  private ObjectMetadata getObjectMetadata(MultipartFile image) {
    ObjectMetadata objectMetadata = new ObjectMetadata();

    objectMetadata.setContentLength(image.getSize());
    objectMetadata.setContentType(image.getContentType());

    return objectMetadata;
  }
}
