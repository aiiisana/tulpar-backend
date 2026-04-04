package kz.diploma.tulpar.dto.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SettingsResponse {
    private String supportEmail;
    private String termsUrl;
    private String privacyPolicyUrl;
    private String appVersion;
}
