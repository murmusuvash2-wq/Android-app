const fs = require('fs');
let js = fs.readFileSync('admin-panel/app.js', 'utf8');

const oldLogic = /const \{ data, error \} = await supabaseClient\.functions\.invoke\('admin-publish-product'[\s\S]*?out\.textContent = "Exception: " \+ e\.message;\n\s*\}/;

const newLogic = `
                                try {
                                    const response = await supabaseClient.functions.invoke('admin-publish-product', { 
                                        body: { draft_id: 'fake-draft-123', updated_data: {} } 
                                    });
                                    
                                    out.textContent = "Result:\\n" + JSON.stringify(response, null, 2);
                                } catch (e) {
                                    out.textContent = "Exception Caught:\\n" + e.name + ": " + e.message;
                                    // if it's an HttpError, it has a context
                                    if (e.context) {
                                        out.textContent += "\\n\\nContext:\\n" + JSON.stringify(e.context, null, 2);
                                    }
                                }
`;

js = js.replace(oldLogic, newLogic.trim());
fs.writeFileSync('admin-panel/app.js', js);
console.log("Fixed the logic.");
