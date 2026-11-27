package com.jaims.privacyneedle.scam;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Fictional examples: legitimate messages require verification too. */
public final class ScamScenarioCatalog {
    private ScamScenarioCatalog() {}

    private static final List<ScamScenario> ALL = Collections.unmodifiableList(Arrays.asList(
        new ScamScenario("job-fee", "JOB OFFER", "Talent Desk · WhatsApp",
            "Congratulations! You have been selected for a remote role. Pay a ₦3,500 activation fee today before we release your interview details.",
            true, "Charging a fee to release interview information is a major warning sign. The pressure to pay immediately increases the risk.",
            "Verify vacancies on the employer's official careers page. Never pay to receive an interview."),
        new ScamScenario("bank-otp", "BANKING", "Account Support · SMS",
            "Your account needs verification. Reply here with the six-digit code that was just sent to you so we can stop an unauthorised debit.",
            true, "One-time passcodes should never be shared with an unsolicited caller or message. The request is attempting to gain account access.",
            "Contact the bank through its app or the number printed on your card, not this message."),
        new ScamScenario("delivery", "DELIVERY", "Parcel Notice · Email",
            "We couldn't deliver your package. Update your payment details at parcel-update-help.example today to prevent return to sender.",
            true, "An unexpected delivery fee and an unfamiliar domain are warning signs of a phishing attempt.",
            "Open your courier's official website independently and check the tracking number."),
        new ScamScenario("normal-meeting", "WORKPLACE", "Project Coordinator · Email",
            "Reminder: our team review is tomorrow at 10:00. The agenda is attached in the shared company workspace. No action is required if you've already accepted.",
            false, "This message has no payment, credential or urgency demand. It could be routine, but its sender and attachments still need checking.",
            "Verify the sender and open documents through your company's approved workspace."),
        new ScamScenario("investment", "INVESTMENT", "Wealth Mentor · Telegram",
            "Our private investment club guarantees 30% returns every seven days. Only five slots left. Send your funds now and screenshot your transfer.",
            true, "Guaranteed unusually high returns and urgent transfers are strong investment-fraud indicators.",
            "Research the firm independently and never rely on screenshots or private chat promises."),
        new ScamScenario("school-notice", "EDUCATION", "Faculty Office · Portal",
            "Your course-registration deadline is Friday. Sign in through the school's student portal by typing its known address into your browser.",
            false, "A deadline notice using a known school portal, without an embedded unknown sign-in link, may be legitimate. Confirm against official announcements.",
            "Use the address you already know or the school's official app rather than a forwarded link."),
        new ScamScenario("romance", "SOCIAL", "New Contact · Instagram",
            "I can't access my wallet while travelling. I know we only met yesterday, but please buy a gift card and message me the code.",
            true, "Unexpected financial requests from a new contact, especially gift-card codes, are classic social-engineering warning signs.",
            "Never send gift-card codes to online contacts. Verify identities through independent channels."),
        new ScamScenario("password-alert", "ACCOUNT SAFETY", "Security Notifications · App",
            "Your account settings show a new device. Review recent sessions by opening the official app from your home screen and checking Settings > Security.",
            false, "The instruction points to the official app rather than asking for credentials through a link. Still independently confirm the alert.",
            "Check the account's security page yourself and remove devices you don't recognise."),
        new ScamScenario("fake-grant", "BUSINESS GRANT", "SME Growth Team · SMS",
            "Your company has been approved for a ₦2 million grant you never applied for. Send your BVN and NIN to claim it within one hour.",
            true, "An unsolicited grant approval and request for sensitive identity data are major warning signs.",
            "Check programme eligibility and applications on the grant provider's verified website."),
        new ScamScenario("fake-support", "TECH SUPPORT", "IT Helpdesk · WhatsApp",
            "Your laptop is infected. Install our remote-control app now and send the access code so our technician can remove the virus.",
            true, "Unsolicited remote-access requests can expose files, credentials and bank sessions.",
            "Contact your employer's recognised IT team or a trusted service provider directly."),
        new ScamScenario("newsletter", "NEWSLETTER", "Privacy Digest · Email",
            "Here's your weekly privacy reading list. If you don't want updates, you can change your preferences from your original subscription account.",
            false, "There is no immediate request for money or passwords. As with any email, verify sender details and avoid unexpected attachments.",
            "Manage subscriptions through the publisher's official website."),
        new ScamScenario("mobile-transfer", "PAYMENTS", "Marketplace Buyer · Chat",
            "I just sent the payment, see my screenshot. Hand over the phone now. My transfer will appear in your account after a system delay.",
            true, "A transfer screenshot is not proof of cleared funds. Pressure to release goods before confirmation can facilitate fraud.",
            "Check your bank's actual available balance and transaction history before releasing goods."),
        new ScamScenario("tax-refund", "TAX NOTICE", "Revenue Desk · SMS",
            "You have a tax refund waiting. Submit your card PIN and full bank login at refund-access.example within 20 minutes.",
            true, "Tax bodies do not need your card PIN or online-banking password to send a refund.",
            "Verify any tax correspondence directly through the relevant government's official portal."),
        new ScamScenario("event-ticket", "EVENT", "Event Organiser · Email",
            "Your ticket confirmation is available in the event platform account you used to book. Sign in through the app you installed.",
            false, "The message directs you to the booking service you already use, with no unexpected payment or credential link.",
            "Open the service independently and compare your booking details."),
        new ScamScenario("friend-urgent", "ACCOUNT TAKEOVER", "Friend · Messenger",
            "Please send ₦50,000 immediately. Don't call; my phone is broken. I'm sending this from a new account and need it in ten minutes.",
            true, "A new account, refusal to verify identity and urgent money request can indicate impersonation.",
            "Call your friend through a number you already know before sending anything."),
        new ScamScenario("workshop-invite", "TRAINING", "Learning Team · Email",
            "You registered for the webinar next week. Visit the organiser's official event calendar to check the session time and joining details.",
            false, "This is consistent with routine registration information, but not proof of authenticity by itself.",
            "Use the official calendar or registration confirmation you originally received.")
    ));

    public static ScamScenario forDay(String day) {
        return ALL.get(ScamChallengeRules.scenarioIndex(day, ALL.size()));
    }

    public static ScamScenario forBonusDay(String day) {
        return ALL.get((ScamChallengeRules.scenarioIndex(day, ALL.size()) + 1) % ALL.size());
    }

    public static ScamScenario forToday() {
        return forDay(ScamChallengeRules.todayUtc());
    }

    public static int size() {
        return ALL.size();
    }
}
