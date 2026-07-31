package com.example.myrajourney.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.myrajourney.R;
import com.example.myrajourney.core.ui.ThemeManager;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Policy");
        }

        // Setup privacy policy content
        TextView tvPrivacyContent = findViewById(R.id.tvPrivacyContent);
        tvPrivacyContent.setText(getPrivacyPolicyContent());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String getPrivacyPolicyContent() {
        return "MYRA JOURNEY - PRIVACY POLICY\n\n" +
                "Last Updated: January 28, 2026\n\n" +
                
                "1. INFORMATION WE COLLECT\n\n" +
                "We collect the following information:\n" +
                "• Personal Information: Name, email address, mobile number, date of birth, address\n" +
                "• Health Information: Medical reports, symptoms, medications, rehabilitation progress\n" +
                "• Usage Data: App usage patterns, device information, IP address\n\n" +
                
                "2. HOW WE USE YOUR INFORMATION\n\n" +
                "Your information is used to:\n" +
                "• Provide healthcare services and track your rehabilitation progress\n" +
                "• Enable communication between patients and healthcare providers\n" +
                "• Send medication reminders and health notifications\n" +
                "• Improve our services and user experience\n\n" +
                
                "3. INFORMATION SHARING\n\n" +
                "We share your information only with:\n" +
                "• Your assigned healthcare providers\n" +
                "• Medical professionals involved in your care\n" +
                "• Emergency contacts when necessary for your safety\n" +
                "We NEVER sell your personal or health information to third parties.\n\n" +
                
                "4. DATA SECURITY\n\n" +
                "We implement industry-standard security measures:\n" +
                "• Encrypted data transmission and storage\n" +
                "• Secure authentication and access controls\n" +
                "• Regular security audits and updates\n" +
                "• HIPAA-compliant data handling procedures\n\n" +
                
                "5. YOUR RIGHTS\n\n" +
                "You have the right to:\n" +
                "• Access your personal and health information\n" +
                "• Request corrections to inaccurate data\n" +
                "• Delete your account and associated data\n" +
                "• Opt-out of non-essential communications\n\n" +
                
                "6. DATA RETENTION\n\n" +
                "We retain your information:\n" +
                "• Personal data: Until account deletion\n" +
                "• Health records: As required by medical regulations (typically 7 years)\n" +
                "• Usage data: Up to 2 years for service improvement\n\n" +
                
                "7. CHILDREN'S PRIVACY\n\n" +
                "Our service is restricted to users 18 years and older. We do not knowingly collect information from minors under 18.\n\n" +
                
                "8. CHANGES TO PRIVACY POLICY\n\n" +
                "We may update this policy periodically. Users will be notified of significant changes through the app or email.\n\n" +
                
                "9. CONTACT INFORMATION\n\n" +
                "For privacy-related questions or concerns:\n" +
                "Email: privacy@myrajourney.com\n" +
                "Phone: +1-800-MYRA-HELP\n" +
                "Address: MyRA Journey Privacy Office\n" +
                "123 Healthcare Blvd, Medical City, MC 12345\n\n" +
                
                "10. CONSENT\n\n" +
                "By using MyRA Journey, you consent to the collection and use of your information as described in this Privacy Policy. " +
                "This consent is mandatory for using our healthcare services.\n\n" +
                
                "© 2026 MyRA Journey. All rights reserved.";
    }
}