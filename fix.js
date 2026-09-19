const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');
js = js.replace(/                \}\n                return; \/\/ Stop execution\n            \}/, `                return; // Stop execution
            }`);
fs.writeFileSync('admin-panel/app.js', js);
