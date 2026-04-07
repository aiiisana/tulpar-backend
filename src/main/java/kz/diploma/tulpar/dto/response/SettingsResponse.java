package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SettingsResponse {
    private String supportEmail;
    private String termsUrl;
    private String privacyPolicyUrl;
    private String appVersion;
}
