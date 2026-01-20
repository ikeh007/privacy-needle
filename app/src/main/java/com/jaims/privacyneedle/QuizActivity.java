package com.jaims.privacyneedle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jaims.privacyneedle.models.QuizQuestion;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    TextView questionText, scoreText;
    Button opt1, opt2, opt3, opt4;

    QuizQuestion todayQuestion;
    boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quiz_activity);

        questionText = findViewById(R.id.questionText);
        scoreText = findViewById(R.id.scoreText);
        opt1 = findViewById(R.id.opt1);
        opt2 = findViewById(R.id.opt2);
        opt3 = findViewById(R.id.opt3);
        opt4 = findViewById(R.id.opt4);

        List<QuizQuestion> questions = getAllQuestions();
        todayQuestion = getQuestionOfTheDay(questions);

        showQuestion();

        opt1.setOnClickListener(v -> checkAnswer(0));
        opt2.setOnClickListener(v -> checkAnswer(1));
        opt3.setOnClickListener(v -> checkAnswer(2));
        opt4.setOnClickListener(v -> checkAnswer(3));
    }

    private void showQuestion() {
        questionText.setText(todayQuestion.question);
        opt1.setText(todayQuestion.options[0]);
        opt2.setText(todayQuestion.options[1]);
        opt3.setText(todayQuestion.options[2]);
        opt4.setText(todayQuestion.options[3]);
        scoreText.setText("📅 Question of the Day");
    }

    private void checkAnswer(int selectedIndex) {
        if (answered) return;

        answered = true;

        if (selectedIndex == todayQuestion.correctIndex) {
            Toast.makeText(this, "✅ Correct! Well done", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ Wrong answer", Toast.LENGTH_SHORT).show();
        }

        saveAttemptForToday();
    }

    private void saveAttemptForToday() {
        SharedPreferences prefs = getSharedPreferences("daily_quiz", MODE_PRIVATE);
        prefs.edit()
                .putInt("last_attempt_day", getToday())
                .apply();
    }

    private QuizQuestion getQuestionOfTheDay(List<QuizQuestion> questions) {
        int index = getToday() % questions.size();
        return questions.get(index);
    }

    private int getToday() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    private List<QuizQuestion> getAllQuestions() {
        List<QuizQuestion> list = new ArrayList<>();

        // 1–10 Basics
        list.add(new QuizQuestion("What does NDPA stand for?",
                new String[]{"Nigeria Data Protection Act", "National Digital Privacy Act", "Network Data Privacy Act", "Nigeria Digital Protection Authority"}, 0));

        list.add(new QuizQuestion("Which body enforces the NDPA?",
                new String[]{"NCC", "NITDA", "NDPC", "EFCC"}, 2));

        list.add(new QuizQuestion("Personal data refers to:",
                new String[]{"Any stored data", "Data relating to an identifiable person", "Only financial data", "Only government data"}, 1));

        list.add(new QuizQuestion("Sensitive personal data includes:",
                new String[]{"Email", "Phone number", "Biometric data", "Office address"}, 2));

        list.add(new QuizQuestion("Who is a Data Controller?",
                new String[]{"Any IT staff", "Person who determines purpose of processing", "Only government", "Data processor"}, 1));

        list.add(new QuizQuestion("A Data Processor is:",
                new String[]{"Owner of data", "Processes data on behalf of controller", "NDPC staff", "Internet provider"}, 1));

        list.add(new QuizQuestion("Which is NOT a lawful basis for processing?",
                new String[]{"Consent", "Legal obligation", "Legitimate interest", "Curiosity"}, 3));

        list.add(new QuizQuestion("Data minimization means:",
                new String[]{"Collect all possible data", "Collect only necessary data", "Store data forever", "Sell unused data"}, 1));

        list.add(new QuizQuestion("Purpose limitation requires data to be:",
                new String[]{"Used for any reason", "Used only for specified purpose", "Shared freely", "Stored indefinitely"}, 1));

        list.add(new QuizQuestion("Accuracy principle means data must be:",
                new String[]{"Encrypted", "Correct and up to date", "Shared publicly", "Deleted monthly"}, 1));

        // 11–30 Rights of Data Subjects
        list.add(new QuizQuestion("Right to access allows data subjects to:",
                new String[]{"Delete data", "Request their data", "Sell data", "Encrypt data"}, 1));

        list.add(new QuizQuestion("Right to rectification allows:",
                new String[]{"Deleting data", "Correcting inaccurate data", "Blocking access", "Selling data"}, 1));

        list.add(new QuizQuestion("Right to erasure is also known as:",
                new String[]{"Right to forget", "Right to sell", "Right to encrypt", "Right to copy"}, 0));

        list.add(new QuizQuestion("Right to data portability allows:",
                new String[]{"Delete data", "Transfer data to another controller", "Encrypt data", "Hide data"}, 1));

        list.add(new QuizQuestion("Right to object applies when processing is based on:",
                new String[]{"Consent", "Legitimate interest", "Contract", "Law"}, 1));

        list.add(new QuizQuestion("Children’s data requires:",
                new String[]{"No protection", "Parental consent", "Public disclosure", "NDPC approval always"}, 1));

        list.add(new QuizQuestion("A data subject is:",
                new String[]{"Data controller", "Identifiable individual", "Company", "Government agency"}, 1));

        list.add(new QuizQuestion("Data subjects can withdraw consent:",
                new String[]{"Never", "At any time", "Only after 1 year", "Only via court"}, 1));

        list.add(new QuizQuestion("Automated decision-making refers to:",
                new String[]{"Manual processing", "Decisions made without human involvement", "Court rulings", "NDPC enforcement"}, 1));

        list.add(new QuizQuestion("Profiling involves:",
                new String[]{"Manual filing", "Automated analysis of personal data", "Data deletion", "Encryption"}, 1));

        // 31–60 Compliance & Security
        list.add(new QuizQuestion("A data breach involves:",
                new String[]{"Authorized sharing", "Unauthorized access or loss", "Data backup", "System upgrade"}, 1));

        list.add(new QuizQuestion("Data breaches must be reported to NDPC within:",
                new String[]{"24 hours", "48 hours", "72 hours", "7 days"}, 2));

        list.add(new QuizQuestion("Encryption helps by:",
                new String[]{"Deleting data", "Making data unreadable to unauthorized users", "Sharing data", "Speed processing"}, 1));

        list.add(new QuizQuestion("Pseudonymization means:",
                new String[]{"Deleting identifiers", "Replacing identifiers", "Publishing data", "Encrypting files"}, 1));

        list.add(new QuizQuestion("DPIA stands for:",
                new String[]{"Data Privacy Impact Assessment", "Data Processing Internal Audit", "Digital Protection Internal Act", "Data Public Information Act"}, 0));

        list.add(new QuizQuestion("DPIA is required when processing is:",
                new String[]{"Low risk", "High risk", "Manual", "Offline"}, 1));

        list.add(new QuizQuestion("Data retention means:",
                new String[]{"Keeping data forever", "Keeping data only as long as needed", "Selling data", "Publishing data"}, 1));

        list.add(new QuizQuestion("Privacy by design means:",
                new String[]{"Add privacy later", "Embed privacy from start", "Ignore privacy", "Manual processing"}, 1));

        list.add(new QuizQuestion("Privacy by default ensures:",
                new String[]{"Maximum data collection", "Minimum necessary data", "Public access", "Unlimited storage"}, 1));

        list.add(new QuizQuestion("Access control ensures:",
                new String[]{"Anyone can access data", "Only authorized users access data", "Data deletion", "Data sharing"}, 1));

        // 61–80 DPO & Governance
        list.add(new QuizQuestion("DPO stands for:",
                new String[]{"Data Privacy Officer", "Data Protection Officer", "Digital Policy Officer", "Data Processing Officer"}, 1));

        list.add(new QuizQuestion("A DPO must act:",
                new String[]{"For profit", "Independently", "For management only", "For IT department"}, 1));

        list.add(new QuizQuestion("Who appoints a DPO?",
                new String[]{"NDPC", "Organization", "Court", "NITDA"}, 1));

        list.add(new QuizQuestion("DPO reports to:",
                new String[]{"IT Manager", "Top management", "HR", "External auditor"}, 1));

        list.add(new QuizQuestion("Training staff on privacy is:",
                new String[]{"Optional", "Mandatory", "Illegal", "Rare"}, 1));

        list.add(new QuizQuestion("Record of processing activities is kept by:",
                new String[]{"Only NDPC", "Controllers and processors", "Only DPO", "Courts"}, 1));

        list.add(new QuizQuestion("Third-party processors require:",
                new String[]{"Verbal agreement", "Data processing agreement", "No agreement", "Court order"}, 1));

        list.add(new QuizQuestion("Cross-border data transfer requires:",
                new String[]{"No condition", "Adequate safeguards", "Public disclosure", "NDPC ownership"}, 1));

        list.add(new QuizQuestion("Adequacy decision refers to:",
                new String[]{"Data deletion", "Country with adequate protection", "Encryption", "Local hosting"}, 1));

        list.add(new QuizQuestion("Audit helps organizations to:",
                new String[]{"Hide breaches", "Assess compliance", "Delete records", "Avoid NDPC"}, 1));

        // 81–100 Advanced & Scenario
        list.add(new QuizQuestion("Lawful interception must be:",
                new String[]{"Secret always", "Authorized by law", "Unlimited", "Manual"}, 1));

        list.add(new QuizQuestion("Consent must be:",
                new String[]{"Forced", "Freely given", "Implied always", "Permanent"}, 1));

        list.add(new QuizQuestion("Silence or inactivity means consent?",
                new String[]{"Yes", "No", "Sometimes", "Only online"}, 1));

        list.add(new QuizQuestion("Data anonymization means:",
                new String[]{"Reversible data masking", "Irreversible removal of identifiers", "Encryption", "Pseudonymization"}, 1));

        list.add(new QuizQuestion("A privacy notice informs users about:",
                new String[]{"Company profit", "Data usage and rights", "System errors", "NDPC rules"}, 1));

        list.add(new QuizQuestion("Incident response plan is used for:",
                new String[]{"Marketing", "Handling data breaches", "Recruitment", "Sales"}, 1));

        list.add(new QuizQuestion("Least privilege principle means:",
                new String[]{"Full access for all", "Minimum access needed", "Public access", "Admin access"}, 1));

        list.add(new QuizQuestion("Monitoring employee data requires:",
                new String[]{"No notice", "Transparency and lawful basis", "Total secrecy", "NDPC ownership"}, 1));

        list.add(new QuizQuestion("Cloud data responsibility lies with:",
                new String[]{"Cloud provider only", "Controller primarily", "Users", "NDPC"}, 1));

        list.add(new QuizQuestion("Final accountability for compliance rests with:",
                new String[]{"DPO only", "Data Controller", "IT staff", "Auditors"}, 1));

        // 101–120 Legal & Regulatory
        list.add(new QuizQuestion("Which regulation replaced the NDPR in Nigeria?",
                new String[]{"Cybercrimes Act", "NDPA 2023", "FOI Act", "NITDA Act"}, 1));

        list.add(new QuizQuestion("NDPA applies to data processed:",
                new String[]{"Only offline", "Only in Nigeria", "Within or outside Nigeria involving Nigerians", "Only government data"}, 2));

        list.add(new QuizQuestion("Which sector is exempt from NDPA?",
                new String[]{"Banking", "Healthcare", "None", "Telecom"}, 2));

        list.add(new QuizQuestion("Data protection laws primarily protect:",
                new String[]{"Companies", "Government", "Individuals", "Hackers"}, 2));

        list.add(new QuizQuestion("Which principle ensures accountability?",
                new String[]{"Transparency", "Fairness", "Accountability", "Security"}, 2));

        list.add(new QuizQuestion("Consent under NDPA must be withdrawn:",
                new String[]{"With penalty", "Easily", "With court order", "After 30 days"}, 1));

        list.add(new QuizQuestion("NDPC replaced which body?",
                new String[]{"NCC", "NITDA", "NDPB", "EFCC"}, 2));

        list.add(new QuizQuestion("Which fine applies for serious NDPA breach?",
                new String[]{"₦1 million", "₦2 million", "Up to 2% of annual revenue", "₦50,000"}, 2));

        list.add(new QuizQuestion("Regulatory audits are conducted by:",
                new String[]{"Courts", "NDPC", "Police", "Auditors only"}, 1));

        list.add(new QuizQuestion("NDPA aligns closely with:",
                new String[]{"HIPAA", "GDPR", "SOX", "PCI DSS"}, 1));

        // 121–140 Processing & Consent
        list.add(new QuizQuestion("Explicit consent is required for:",
                new String[]{"Marketing emails", "Sensitive personal data", "Website cookies", "Analytics"}, 1));

        list.add(new QuizQuestion("Consent must be documented to prove:",
                new String[]{"Payment", "Compliance", "Ownership", "Identity"}, 1));

        list.add(new QuizQuestion("Pre-ticked boxes indicate:",
                new String[]{"Valid consent", "Invalid consent", "Legal obligation", "Contract"}, 1));

        list.add(new QuizQuestion("Processing without consent is allowed when:",
                new String[]{"Marketing", "Legal obligation exists", "Curiosity", "Research only"}, 1));

        list.add(new QuizQuestion("Lawful processing must be:",
                new String[]{"Secret", "Fair and transparent", "Fast", "Encrypted"}, 1));

        list.add(new QuizQuestion("Consent obtained under duress is:",
                new String[]{"Valid", "Invalid", "Temporary", "Conditional"}, 1));

        list.add(new QuizQuestion("Opt-in consent means:",
                new String[]{"Automatic consent", "User actively agrees", "Implied consent", "Silent consent"}, 1));

        list.add(new QuizQuestion("Opt-out consent is:",
                new String[]{"Best practice", "Discouraged", "Mandatory", "Required"}, 1));

        list.add(new QuizQuestion("Consent records should include:",
                new String[]{"Time and purpose", "Salary", "Address only", "Password"}, 0));

        list.add(new QuizQuestion("Consent should be renewed when:",
                new String[]{"Purpose changes", "Time passes", "Data is stored", "System upgrades"}, 0));

        // 141–160 Security Measures
        list.add(new QuizQuestion("Technical security measures include:",
                new String[]{"Policies", "Encryption", "Training", "Notices"}, 1));

        list.add(new QuizQuestion("Organizational measures include:",
                new String[]{"Firewalls", "Policies and training", "Passwords", "Servers"}, 1));

        list.add(new QuizQuestion("Access logs help to:",
                new String[]{"Slow systems", "Track data access", "Delete data", "Encrypt files"}, 1));

        list.add(new QuizQuestion("Two-factor authentication improves:",
                new String[]{"Speed", "Security", "Storage", "Compliance cost"}, 1));

        list.add(new QuizQuestion("Data backup protects against:",
                new String[]{"Legal fines", "Data loss", "Consent withdrawal", "Audits"}, 1));

        list.add(new QuizQuestion("Incident detection systems are used to:",
                new String[]{"Market products", "Detect breaches early", "Encrypt emails", "Train staff"}, 1));

        list.add(new QuizQuestion("Physical security includes:",
                new String[]{"CCTV", "Encryption", "Firewalls", "Cookies"}, 0));

        list.add(new QuizQuestion("Secure disposal includes:",
                new String[]{"Archiving", "Shredding", "Publishing", "Selling"}, 1));

        list.add(new QuizQuestion("Weak passwords increase risk of:",
                new String[]{"Compliance", "Unauthorized access", "Audit success", "Encryption"}, 1));

        list.add(new QuizQuestion("Security testing should be:",
                new String[]{"Rare", "Periodic", "Optional", "Avoided"}, 1));

        // 161–180 Processors & Third Parties
        list.add(new QuizQuestion("Processors must act on:",
                new String[]{"Their own purpose", "Controller instructions", "Public interest", "Marketing needs"}, 1));

        list.add(new QuizQuestion("Processor liability arises when:",
                new String[]{"They follow instructions", "They violate NDPA", "They encrypt data", "They report breaches"}, 1));

        list.add(new QuizQuestion("Vendor risk assessment evaluates:",
                new String[]{"Price only", "Privacy and security risks", "Brand popularity", "Location"}, 1));

        list.add(new QuizQuestion("Outsourcing processing requires:",
                new String[]{"Verbal trust", "Written agreement", "Public notice", "Court order"}, 1));

        list.add(new QuizQuestion("Sub-processors require:",
                new String[]{"No approval", "Controller authorization", "Public listing", "NDPC consent"}, 1));

        list.add(new QuizQuestion("Cross-border transfer safeguards include:",
                new String[]{"Encryption and contracts", "Speed", "Public access", "Unlimited sharing"}, 0));

        list.add(new QuizQuestion("Standard contractual clauses are used for:",
                new String[]{"Local storage", "International transfers", "Encryption", "Audits"}, 1));

        list.add(new QuizQuestion("Third-party audits help to:",
                new String[]{"Avoid NDPA", "Verify compliance", "Hide breaches", "Delete data"}, 1));

        list.add(new QuizQuestion("Cloud providers usually act as:",
                new String[]{"Controllers", "Processors", "Regulators", "Auditors"}, 1));

        list.add(new QuizQuestion("Joint controllers share:",
                new String[]{"Liability and responsibility", "Passwords", "Servers", "Staff"}, 0));

        // 181–200 Ethics & Advanced Scenarios
        list.add(new QuizQuestion("Ethical data use prioritizes:",
                new String[]{"Profit", "Individual rights", "Speed", "Automation"}, 1));

        list.add(new QuizQuestion("Dark patterns are used to:",
                new String[]{"Improve UX", "Manipulate consent", "Secure data", "Encrypt systems"}, 1));

        list.add(new QuizQuestion("Privacy fatigue refers to:",
                new String[]{"Too much security", "Users ignoring notices", "System overload", "Slow processing"}, 1));

        list.add(new QuizQuestion("Algorithm bias occurs when:",
                new String[]{"Data is encrypted", "Decisions unfairly disadvantage groups", "Audits fail", "Servers crash"}, 1));

        list.add(new QuizQuestion("AI systems require DPIA when:",
                new String[]{"Low risk", "High-risk profiling exists", "Offline", "Manual"}, 1));

        list.add(new QuizQuestion("Transparency requires controllers to:",
                new String[]{"Hide processing", "Inform data subjects", "Encrypt notices", "Avoid questions"}, 1));

        list.add(new QuizQuestion("Data ethics goes beyond:",
                new String[]{"Law", "Minimum legal compliance", "Security", "Documentation"}, 1));

        list.add(new QuizQuestion("Automated rejection decisions require:",
                new String[]{"No review", "Human intervention option", "Court approval", "Encryption"}, 1));

        list.add(new QuizQuestion("Training frequency should be:",
                new String[]{"Once ever", "Regular", "Optional", "Rare"}, 1));

        list.add(new QuizQuestion("Strong governance improves:",
                new String[]{"Risk management", "Breaches", "Fines", "Exposure"}, 0));

        // 201–220 Data Subject Rights
        list.add(new QuizQuestion("Under NDPA, data subjects can request data correction when:",
                new String[]{"They want new data", "Their data is inaccurate", "They like the company", "They own the company"}, 1));

        list.add(new QuizQuestion("The right to data portability allows:",
                new String[]{"Deleting data", "Transferring data to another provider", "Selling data", "Ignoring data"}, 1));

        list.add(new QuizQuestion("Data subjects can withdraw consent:",
                new String[]{"Only once", "At any time", "Only after 1 year", "Never"}, 1));

        list.add(new QuizQuestion("Data access requests must be responded to:",
                new String[]{"Immediately", "Within a reasonable time", "After 1 year", "Only if paid"}, 1));

        list.add(new QuizQuestion("Right to be forgotten means:",
                new String[]{"Delete all company data", "Delete personal data on request", "Delete government data", "Delete all records"}, 1));

        list.add(new QuizQuestion("The right to object applies mainly to:",
                new String[]{"Legal obligation", "Marketing and profiling", "Data encryption", "System upgrades"}, 1));

        list.add(new QuizQuestion("Data subject rights should be included in:",
                new String[]{"Privacy policy", "Internal memo", "Financial report", "Employee manual"}, 0));

        list.add(new QuizQuestion("When processing is lawful, controllers must still:",
                new String[]{"Ignore requests", "Provide clear notices", "Hide information", "Delay responses"}, 1));

        list.add(new QuizQuestion("Data subject rights apply to:",
                new String[]{"Only employees", "Any individual", "Only citizens", "Only adults"}, 1));

        list.add(new QuizQuestion("Data access request is also known as:",
                new String[]{"SAR", "DPIA", "POPIA", "NDA"}, 0));

        // 221–240 DPIA & Risk Assessment
        list.add(new QuizQuestion("DPIA is required when processing poses:",
                new String[]{"Low risk", "High risk", "No risk", "Unknown risk"}, 1));

        list.add(new QuizQuestion("DPIA should be done:",
                new String[]{"Once only", "Before processing begins", "After breach", "Never"}, 1));

        list.add(new QuizQuestion("A DPIA includes:",
                new String[]{"Risk identification", "Marketing strategy", "Sales forecast", "Product roadmap"}, 0));

        list.add(new QuizQuestion("Risk mitigation includes:",
                new String[]{"Ignoring risk", "Reducing or eliminating risk", "Increasing risk", "Selling risk"}, 1));

        list.add(new QuizQuestion("Residual risk is:",
                new String[]{"Risk eliminated", "Risk remaining after controls", "Risk ignored", "Risk documented"}, 1));

        list.add(new QuizQuestion("A DPIA should be reviewed:",
                new String[]{"Annually or after major change", "Never", "Only once", "After 5 years"}, 0));

        list.add(new QuizQuestion("High-risk processing includes:",
                new String[]{"Newsletter emails", "Biometric profiling", "Manual filing", "Public data"}, 1));

        list.add(new QuizQuestion("DPIA outcomes should be:",
                new String[]{"Secret", "Documented and approved", "Deleted", "Shared publicly"}, 1));

        list.add(new QuizQuestion("Data risk register is used to:",
                new String[]{"Track risks and controls", "Store user passwords", "Track employees only", "Store invoices"}, 0));

        list.add(new QuizQuestion("Risk appetite defines:",
                new String[]{"How much risk an organization accepts", "Data storage limit", "Number of users", "Budget"}, 0));

        // 241–260 Data Governance
        list.add(new QuizQuestion("Data governance ensures:",
                new String[]{"Uncontrolled access", "Structured data management", "No policies", "No audits"}, 1));

        list.add(new QuizQuestion("Data classification helps to:",
                new String[]{"Identify sensitivity levels", "Increase risk", "Decrease security", "Ignore compliance"}, 0));

        list.add(new QuizQuestion("Data retention policies define:",
                new String[]{"How long data is kept", "How data is encrypted", "How data is shared", "How data is deleted"}, 0));

        list.add(new QuizQuestion("A data inventory lists:",
                new String[]{"All personal data processed", "Only financial data", "Only employee data", "Only marketing data"}, 0));

        list.add(new QuizQuestion("A data controller is responsible for:",
                new String[]{"Purpose and means of processing", "Only storage", "Only security", "Only marketing"}, 0));

        list.add(new QuizQuestion("Data governance framework includes:",
                new String[]{"Roles, policies, processes", "Only software", "Only hardware", "Only staff training"}, 0));

        list.add(new QuizQuestion("A data retention schedule should be:",
                new String[]{"Defined, reviewed, enforced", "Hidden", "Never reviewed", "Ignored"}, 0));

        list.add(new QuizQuestion("Data breach response plan should include:",
                new String[]{"Notification steps", "Marketing plan", "Hiring plan", "Budget plan"}, 0));

        list.add(new QuizQuestion("A DPO is typically responsible for:",
                new String[]{"Compliance monitoring", "Sales targets", "Customer service", "Server maintenance"}, 0));

        list.add(new QuizQuestion("A privacy policy should be:",
                new String[]{"Long and complex", "Clear and user-friendly", "Secret", "Only for lawyers"}, 1));

        // 261–280 Breach & Incident Management
        list.add(new QuizQuestion("A breach is defined as:",
                new String[]{"Only cyber attack", "Any unauthorized disclosure", "Only physical theft", "Only internal error"}, 1));

        list.add(new QuizQuestion("Breach notification should be:",
                new String[]{"Delayed", "Timely and accurate", "Ignored", "Hidden"}, 1));

        list.add(new QuizQuestion("Breach investigation should include:",
                new String[]{"Root cause analysis", "Marketing analysis", "Sales report", "No documentation"}, 0));

        list.add(new QuizQuestion("Incident response teams should be:",
                new String[]{"Undefined", "Predefined with roles", "Only managers", "Only IT"}, 1));

        list.add(new QuizQuestion("Containment in breach response means:",
                new String[]{"Stop further damage", "Ignore the incident", "Publicly disclose immediately", "Delete all data"}, 0));

        list.add(new QuizQuestion("A breach log should include:",
                new String[]{"What happened and actions taken", "Only dates", "Only names", "Only budgets"}, 0));

        list.add(new QuizQuestion("Breach severity is assessed based on:",
                new String[]{"Impact on individuals", "Company revenue only", "Number of employees", "System age"}, 0));

        list.add(new QuizQuestion("Post-breach review aims to:",
                new String[]{"Improve security and controls", "Blame staff", "Delete evidence", "Ignore"}, 0));

        list.add(new QuizQuestion("Forensics should be performed by:",
                new String[]{"Qualified experts", "Anyone", "Marketing staff", "Interns"}, 0));

        list.add(new QuizQuestion("Breach notification must be sent to:",
                new String[]{"Only the CEO", "Regulator and affected individuals", "Only employees", "Only vendors"}, 1));

        // 281–300 Emerging Topics & Practical Scenarios
        list.add(new QuizQuestion("Privacy by design requires:",
                new String[]{"Privacy added after development", "Privacy embedded from start", "Privacy ignored", "Privacy outsourced"}, 1));

        list.add(new QuizQuestion("Privacy by default means:",
                new String[]{"Highest privacy settings are default", "No privacy settings", "Default open sharing", "Only admin can see"}, 0));

        list.add(new QuizQuestion("Data minimization means:",
                new String[]{"Collect only necessary data", "Collect everything", "Collect only public data", "Collect only employee data"}, 0));

        list.add(new QuizQuestion("A data breach may happen due to:",
                new String[]{"Human error", "Cyber attack", "Insider threat", "All of the above"}, 3));

        list.add(new QuizQuestion("Anonymization differs from pseudonymization because:",
                new String[]{"Anonymized data cannot be re-identified", "Pseudonymized data is deleted", "Anonymized data is visible", "No difference"}, 0));

        list.add(new QuizQuestion("A privacy notice should be updated when:",
                new String[]{"Processing changes", "Company name changes only", "Nothing changes", "Staff changes"}, 0));

        list.add(new QuizQuestion("Data subject requests must be handled:",
                new String[]{"Only if paid", "Free of charge", "Only for employees", "Only for citizens"}, 1));

        list.add(new QuizQuestion("Consent cannot be valid if:",
                new String[]{"Given freely", "Given under pressure", "Given with clear information", "Given willingly"}, 1));

        list.add(new QuizQuestion("Data sharing agreements should include:",
                new String[]{"Purpose, roles, security, retention", "Only price", "Only signatures", "Only dates"}, 0));

        list.add(new QuizQuestion("A privacy training program should be:",
                new String[]{"Ongoing and measurable", "One-time", "Optional", "Unstructured"}, 0));

        return list;

        }
    }