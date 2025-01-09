package com.potato_y.timely.aws;

import com.amazonaws.services.cloudfront.CloudFrontUrlSigner;
import com.amazonaws.services.cloudfront.util.SignerUtils.Protocol;
import java.io.File;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CloudFrontService {

  @Value("${aws.cloud-front.domain}")
  private String domain;

  @Value("${aws.cloud-front.private-key-path}")
  private String privateKeyPath;

  @Value("${aws.cloud-front.key-id}")
  private String keyId;

  public String generateSignedUrl(String objectKey) throws IOException, InvalidKeySpecException {
    Date expiration = new Date(System.currentTimeMillis() + (1000 * 60 * 20));
    File privateKey = new ClassPathResource(privateKeyPath).getFile();

    return CloudFrontUrlSigner.getSignedURLWithCannedPolicy(
        Protocol.https,
        domain,
        privateKey,
        objectKey,
        keyId,
        expiration
    );
  }
}
