import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.*
import com.cloudbees.jenkins.plugins.awscredentials.*
import org.jenkinsci.plugins.plaincredentials.impl.*
import hudson.util.Secret

def credsFile = new File("/var/jenkins_home/creds-export.txt") // Adjust path
if (!credsFile.exists()) {
    println "❌ Credentials file not found: ${credsFile.absolutePath}"
    return
}

def upsertCredential(Credentials c) {
    def store = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0].getStore()
    def domain = Domain.global()
    def existing = store.getCredentials(domain).find { it.id == c.id }
    if (existing) {
        println "Updating credential: ${c.id}"
        store.updateCredentials(domain, existing, c)
    } else {
        println "Adding credential: ${c.id}"
        store.addCredentials(domain, c)
    }
}

// Parse the file into a list of maps (one map per credential)
def credentialsToImport = []
def current = [:]
credsFile.eachLine { line ->
    line = line.trim()
    if (line.isEmpty()) {
        if (current) {
            credentialsToImport << current
            current = [:]
        }
    } else {
        def parts = line.split("=", 2)
        if (parts.length == 2) {
            // Handle multi-line private key concatenation
            if (parts[0] == "private_key" && current.containsKey("private_key")) {
                // Append this line to previous private_key with newline
                current["private_key"] += "\n" + parts[1]
            } else {
                current[parts[0]] = parts[1]
            }
        } else if (line.startsWith("-----BEGIN RSA PRIVATE KEY-----") || line.startsWith("-----END RSA PRIVATE KEY-----")) {
            // Handle multiline private key parts without '=' sign
            current["private_key"] = (current["private_key"] ?: "") + (current["private_key"] ? "\n" : "") + line
        } else if (current.containsKey("private_key")) {
            // Assume continuation of private key block without '=' sign
            current["private_key"] += "\n" + line
        }
    }
}
// Add last cred if file does not end with blank line
if (current) {
    credentialsToImport << current
}

credentialsToImport.each { cred ->

    switch (cred["type"]) {
        case "username_password":
        case "user/password":
            def userCred = new UsernamePasswordCredentialsImpl(
                CredentialsScope.GLOBAL,
                cred["id"],
                cred["description"] ?: "",
                cred["username"],
                cred["password"]
            )
            upsertCredential(userCred)
            break

        case "secret_text":
        case "secret text":
            def secretText = new StringCredentialsImpl(
                CredentialsScope.GLOBAL,
                cred["id"],
                cred["description"] ?: "",
                Secret.fromString(cred["secret"])
            )
            upsertCredential(secretText)
            break

        case "ssh_key":
        case "ssh priv key":
            def sshCred = new BasicSSHUserPrivateKey(
                CredentialsScope.GLOBAL,
                cred["id"],
                cred["username"] ?: "",
                new BasicSSHUserPrivateKey.DirectEntryPrivateKeySource(cred["private_key"]),
                cred["passphrase"] ?: "",
                cred["description"] ?: ""
            )
            upsertCredential(sshCred)
            break

        case "aws":
            def awsCred = new AWSCredentialsImpl(
                CredentialsScope.GLOBAL,
                cred["id"],
                cred["access_key"],
                cred["secret_key"],
                cred["description"] ?: ""
            )
            upsertCredential(awsCred)
            break

        case "secret_file":
            println "Secret file type not supported for import (id: ${cred['id']})"
            break

        default:
            println "Unsupported credential type: ${cred['type']} (id: ${cred['id']})"
    }
}

println "All credentials imported successfully."
 