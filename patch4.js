const fs = require('fs');
let code = fs.readFileSync('admin-panel/app.js', 'utf8');

code = code.replace(/loginForm\.addEventListener\('submit', async \(e\) => \{[\s\S]*?\}\);/, `loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('email-input').value;
    const password = document.getElementById('password-input').value;
    const errorEl = document.getElementById('auth-error');
    errorEl.classList.add('hidden');

    try {
        const { data, error } = await supabaseClient.auth.signInWithPassword({ email, password });
        if (error) {
            errorEl.textContent = error.message;
            errorEl.classList.remove('hidden');
        }
    } catch (err) {
        console.error("Login crash:", err);
        errorEl.textContent = err.message || "Failed to sign in.";
        errorEl.classList.remove('hidden');
    }
});`);

fs.writeFileSync('admin-panel/app.js', code);
console.log("Patched loginForm");
