package com.example.ui

object Localization {
    private val hindiStrings = mapOf(
        "app_name" to "सोनू टेंट हाउस",
        "worker_mgmt_sys" to "कर्मचारी प्रबंधन प्रणाली",
        "dashboard" to "डैशबोर्ड",
        "workers" to "कर्मचारी",
        "attendance" to "उपस्थिति",
        "advances" to "अग्रिम भुगतान",
        "payments" to "वेतन भुगतान",
        "reports" to "रिपोर्ट्स",
        "settings" to "सेटिंग्स",
        
        // Dashboard
        "total_workers" to "कुल कर्मचारी",
        "present_today" to "आज उपस्थित",
        "absent_today" to "आज अनुपस्थित",
        "workers_night_shift" to "नाइट शिफ्ट में",
        "todays_total_salary" to "आज का कुल वेतन",
        "todays_advance_given" to "आज दिया अग्रिम",
        "pending_salary" to "बकाया वेतन",
        "total_advance_outstanding" to "कुल बकाया अग्रिम",
        
        // Quick Actions
        "quick_actions" to "त्वरित विकल्प",
        "add_worker" to "नया कर्मचारी",
        "mark_attendance" to "हाजिरी लगाएं",
        "give_advance" to "अग्रिम भुगतान दें",
        "pay_salary" to "वेतन भुगतान करें",
        "salary_report" to "वेतन रिपोर्ट",
        
        // Workers Screen
        "worker_list" to "कर्मचारियों की सूची",
        "search_worker" to "कर्मचारी खोजें...",
        "active" to "सक्रिय",
        "inactive" to "निष्क्रिय",
        "daily_wage" to "दैनिक मजदूरी",
        "night_wage" to "नाइट शिफ्ट मजदूरी",
        "half_wage" to "हाफ डे मजदूरी",
        "skill_category" to "कार्य श्रेणी",
        "joining_date" to "जुड़ने की तिथि",
        "mobile_no" to "मोबाइल नंबर",
        "address" to "पता",
        "emergency_contact" to "आपातकालीन संपर्क",
        "notes" to "टिप्पणी",
        "worker_details" to "कर्मचारी का विवरण",
        "edit_worker" to "विवरण बदलें",
        "delete_worker" to "कर्मचारी हटाएं",
        "archive_worker" to "आर्काइव करें",
        
        // Attendance
        "attendance_status" to "उपस्थिति स्थिति",
        "check_in" to "आने का समय",
        "check_out" to "जाने का समय",
        "hours" to "घंटे",
        "remarks" to "रिमार्क",
        "absence_reason" to "अनुपस्थिति का कारण",
        "sick" to "बीमारी",
        "personal_work" to "व्यक्तिगत कार्य",
        "village" to "गांव गए हैं",
        "family_function" to "पारिवारिक समारोह",
        "emergency" to "आपातकाल",
        "other" to "अन्य",
        "save_attendance" to "उपस्थिति सहेजें",
        
        // Attendance States
        "Present" to "उपस्थित",
        "Absent" to "अनुपस्थित",
        "Half Day" to "हाफ डे",
        "Night Shift" to "नाइट शिफ्ट",
        "Leave" to "छुट्टी",
        "Holiday" to "अवकाश",
        
        // Advance
        "add_advance" to "नया अग्रिम जोड़ें",
        "amount" to "राशि",
        "date" to "तिथि",
        "reason" to "कारण",
        "advance_history" to "अग्रिम का इतिहास",
        
        // Reasons for advance
        "Medical" to "चिकित्सा (Medical)",
        "Travel" to "यात्रा (Travel)",
        "Home Expense" to "घर का खर्च (Home)",
        "Festival" to "त्यौहार (Festival)",
        "Personal" to "व्यक्तिगत (Personal)",
        "Food" to "भोजन (Food)",
        
        // Payments
        "add_payment" to "नया भुगतान दर्ज करें",
        "payment_method" to "भुगतान का माध्यम",
        "Cash" to "नकद (Cash)",
        "UPI" to "UPI (फोनपे/गूगलपे)",
        "Bank" to "बैंक ट्रांसफर (Bank)",
        "paid_amount" to "भुगतान राशि",
        "payment_history" to "भुगतान का इतिहास",
        "remaining_salary" to "बकाया शेष वेतन",
        "total_paid" to "कुल भुगतान",
        
        // Calendar
        "calendar_view" to "कैलेंडर दृश्य",
        "tap_date_info" to "विवरण के लिए किसी तारीख पर टैप करें",
        
        // Settings & General
        "business_name" to "व्यवसाय का नाम",
        "currency" to "मुद्रा",
        "attendance_time" to "उपस्थिति समय",
        "language" to "भाषा",
        "theme" to "थीम",
        "save" to "सहेजें",
        "cancel" to "रद्द करें",
        "confirm_delete" to "हटाने की पुष्टि करें",
        "confirm_delete_msg" to "क्या आप वाकई इस रिकॉर्ड को हटाना चाहते हैं? यह क्रिया वापस नहीं ली जा सकती।",
        "yes" to "हाँ",
        "no" to "नहीं",
        "admin_login" to "एडमिन लॉगिन",
        "login" to "लॉगिन करें",
        "email" to "ईमेल आईडी",
        "password" to "पासवर्ड",
        "forgot_password" to "पासवर्ड भूल गए?",
        "change_password" to "पासवर्ड बदलें",
        "logout" to "लॉगआउट करें",
        "invalid_credentials" to "गलत ईमेल या पासवर्ड",
        "success" to "सफलता",
        "error" to "त्रुटि",
        
        // Reports
        "generate_report" to "रिपोर्ट तैयार करें",
        "today" to "आज",
        "weekly" to "साप्ताहिक",
        "monthly" to "मासिक",
        "yearly" to "वार्षिक",
        "custom_date" to "कस्टम तिथि",
        "export" to "एक्सपोर्ट करें",
        "attendance_pct" to "उपस्थिति प्रतिशत",
        "total_wages" to "कुल मजदूरी",
        "total_advances" to "कुल अग्रिम",
        "net_payout" to "नेट भुगतान",
        "top_workers" to "शीर्ष कामकाजी कर्मचारी",
        "highest_advances" to "सर्वाधिक अग्रिम वाले कर्मचारी"
    )

    private val englishStrings = mapOf(
        "app_name" to "Sonu Tent House",
        "worker_mgmt_sys" to "Worker Management System",
        "dashboard" to "Dashboard",
        "workers" to "Workers",
        "attendance" to "Attendance",
        "advances" to "Advances",
        "payments" to "Payments",
        "reports" to "Reports",
        "settings" to "Settings",
        
        // Dashboard
        "total_workers" to "Total Workers",
        "present_today" to "Present Today",
        "absent_today" to "Absent Today",
        "workers_night_shift" to "On Night Shift",
        "todays_total_salary" to "Today's Salary",
        "todays_advance_given" to "Today's Advance",
        "pending_salary" to "Pending Salary",
        "total_advance_outstanding" to "Total Advance Outstanding",
        
        // Quick Actions
        "quick_actions" to "Quick Actions",
        "add_worker" to "Add Worker",
        "mark_attendance" to "Mark Attendance",
        "give_advance" to "Give Advance",
        "pay_salary" to "Pay Salary",
        "salary_report" to "Salary Report",
        
        // Workers Screen
        "worker_list" to "Worker Directory",
        "search_worker" to "Search worker by name, ID...",
        "active" to "Active",
        "inactive" to "Inactive",
        "daily_wage" to "Daily Wage",
        "night_wage" to "Night Shift Wage",
        "half_wage" to "Half Day Wage",
        "skill_category" to "Skill Category",
        "joining_date" to "Joining Date",
        "mobile_no" to "Mobile Number",
        "address" to "Address",
        "emergency_contact" to "Emergency Contact",
        "notes" to "Notes",
        "worker_details" to "Worker Profile",
        "edit_worker" to "Edit Worker",
        "delete_worker" to "Delete Worker",
        "archive_worker" to "Archive Worker",
        
        // Attendance
        "attendance_status" to "Attendance Status",
        "check_in" to "Check-in Time",
        "check_out" to "Check-out Time",
        "hours" to "Total Hours",
        "remarks" to "Remarks",
        "absence_reason" to "Absence Reason",
        "sick" to "Sick",
        "personal_work" to "Personal Work",
        "village" to "Village / Out of Town",
        "family_function" to "Family Function",
        "emergency" to "Emergency",
        "other" to "Other",
        "save_attendance" to "Save Attendance",
        
        // Attendance States
        "Present" to "Present",
        "Absent" to "Absent",
        "Half Day" to "Half Day",
        "Night Shift" to "Night Shift",
        "Leave" to "Leave",
        "Holiday" to "Holiday",
        
        // Advance
        "add_advance" to "Give Cash Advance",
        "amount" to "Amount",
        "date" to "Date",
        "reason" to "Reason",
        "advance_history" to "Advance History",
        
        // Reasons for advance
        "Medical" to "Medical",
        "Travel" to "Travel",
        "Home Expense" to "Home Expense",
        "Festival" to "Festival",
        "Personal" to "Personal",
        "Food" to "Food",
        
        // Payments
        "add_payment" to "Record Salary Payment",
        "payment_method" to "Payment Method",
        "Cash" to "Cash",
        "UPI" to "UPI (PhonePe/GPay)",
        "Bank" to "Bank Transfer",
        "paid_amount" to "Paid Amount",
        "payment_history" to "Payment History",
        "remaining_salary" to "Remaining Salary",
        "total_paid" to "Total Paid",
        
        // Calendar
        "calendar_view" to "Calendar View",
        "tap_date_info" to "Tap any date to view details",
        
        // Settings & General
        "business_name" to "Business Name",
        "currency" to "Currency",
        "attendance_time" to "Standard Shift Time",
        "language" to "Language",
        "theme" to "Theme Mode",
        "save" to "Save Settings",
        "cancel" to "Cancel",
        "confirm_delete" to "Confirm Deletion",
        "confirm_delete_msg" to "Are you sure you want to delete this record? This action cannot be undone.",
        "yes" to "Yes",
        "no" to "No",
        "admin_login" to "Admin Login",
        "login" to "Login",
        "email" to "Email Address",
        "password" to "Password",
        "forgot_password" to "Forgot Password?",
        "change_password" to "Change Password",
        "logout" to "Logout",
        "invalid_credentials" to "Invalid email or password",
        "success" to "Success",
        "error" to "Error",
        
        // Reports
        "generate_report" to "Generate Report",
        "today" to "Today",
        "weekly" to "Weekly",
        "monthly" to "Monthly",
        "yearly" to "Yearly",
        "custom_date" to "Custom Date",
        "export" to "Export Data",
        "attendance_pct" to "Attendance %",
        "total_wages" to "Total Earned Salary",
        "total_advances" to "Total Advances Given",
        "net_payout" to "Net Payout Status",
        "top_workers" to "Top Active Workers",
        "highest_advances" to "Highest Advance Drawers"
    )

    fun translate(key: String, language: String): String {
        return if (language == "Hindi") {
            hindiStrings[key] ?: englishStrings[key] ?: key
        } else {
            englishStrings[key] ?: key
        }
    }
}
