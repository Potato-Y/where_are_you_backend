package com.potato_y.timely.location.converter;

import com.potato_y.timely.error.exception.BadRequestException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.security.Key;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;

@Converter
public class LocationEncryptConverter implements AttributeConverter<Double, String> {

  @Value("${encryption.key}")
  private String secretKey;

  private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
  private static final byte[] IV = new byte[16];

  @Override
  public String convertToDatabaseColumn(Double attribute) {
    if (attribute == null) {
      return null;
    }

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
      cipher.init(Cipher.ENCRYPT_MODE, generateKey(), ivParameterSpec);
      return Base64.getEncoder().encodeToString(
          cipher.doFinal(attribute.toString().getBytes()));
    } catch (Exception e) {
      throw new BadRequestException("암호화 중 문제가 발생했습니다");
    }
  }

  @Override
  public Double convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      IvParameterSpec ivParameterSpec = new IvParameterSpec(IV);
      cipher.init(Cipher.DECRYPT_MODE, generateKey(), ivParameterSpec);
      byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(dbData));
      return Double.valueOf(new String(decrypted));
    } catch (Exception e) {
      throw new BadRequestException("복호화 중 문제가 발생했습니다");
    }
  }

  private Key generateKey() {
    try {
      return new SecretKeySpec(secretKey.getBytes(), "AES");
    } catch (Exception e) {
      throw new BadRequestException("Key를 생성하는 중 오류가 발생햇습니다");
    }
  }
}
