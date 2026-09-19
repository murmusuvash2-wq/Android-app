const fs = require('fs');
let code = fs.readFileSync('admin-panel/app.js', 'utf8');

code = code.replace(/async function checkSession\(\) \{[\s\S]*?\n\}/, `async function checkSession() {
    try {
        const { data, error } = await supabaseClient.auth.getSession();
        if (error) throw error;
        if (data?.session) {
            showWorkspace(data.session.user);
        } else {
            showAuth();
        }
    } catch (err) {
        console.error("Session check failed:", err);
        showAuth();
    }
}`);

fs.writeFileSync('admin-panel/app.js', code);
console.log("Patched checkSession");
