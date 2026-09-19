const fs = require('fs');
let code = fs.readFileSync('admin-panel/app.js', 'utf8');

const newShowWorkspace = `async function showWorkspace(user) {
    try {
        console.log("showWorkspace started for user", user?.email);
        const authSec = document.getElementById('auth-section');
        const workspaceSec = document.getElementById('workspace-section');
        const emailSpan = document.getElementById('user-email');
        const logout = document.getElementById('logout-btn');
        
        if (authSec) authSec.classList.add('hidden');
        if (emailSpan && user) emailSpan.textContent = user.email || 'Admin';
        if (logout) logout.classList.remove('hidden');
        
        // --- ADMIN AUTHORIZATION QUERY ---
        try {
            const { data: adminData, error: adminErr } = await supabaseClient
                .from('admins')
                .select('*')
                .eq('id', user.id)
                .single();
                
            if (adminErr || !adminData) {
                console.warn("Admin authorization failed:", adminErr);
                if (workspaceSec) {
                    workspaceSec.innerHTML = '<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">Access Denied: You do not have administrator privileges.</div>';
                    workspaceSec.classList.remove('hidden');
                }
                return; // Stop execution
            }
        } catch (err) {
            console.error("Crash during admin check:", err);
            if (workspaceSec) {
                workspaceSec.innerHTML = '<div class="p-6 bg-red-50 text-red-700 rounded-xl border border-red-200">System Error during authorization check.</div>';
                workspaceSec.classList.remove('hidden');
            }
            return;
        }
        
        if (workspaceSec) workspaceSec.classList.remove('hidden');
        
        // Wrap loadDrafts so it cannot crash this function
        loadDrafts().catch(err => console.error("loadDrafts unhandled:", err));
    } catch(err) {
        console.error("Critical error in showWorkspace:", err);
        alert("UI Error: " + err.message);
    }
}`;

code = code.replace(/async function showWorkspace\(user\) \{[\s\S]*?\}\n\}/, newShowWorkspace);

fs.writeFileSync('admin-panel/app.js', code);
console.log("Patched showWorkspace with Admin Check");
