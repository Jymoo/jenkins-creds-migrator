import jenkins.model.*
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.impl.*
import com.cloudbees.plugins.credentials.domains.*
import com.cloudbees.jenkins.plugins.sshcredentials.impl.BasicSSHUserPrivateKey
import com.cloudbees.jenkins.plugins.awscredentials.AWSCredentialsImpl
import org.jenkinsci.plugins.plaincredentials.StringCredentials
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl
import hudson.util.Secret

def outputFile = new File("/var/jenkins_home/creds-export.txt") // Adjust path as needed
outputFile.withWriter('UTF-8') { writer ->

    // get global domain store
    def domainName = null
    def credentialsStore = Jenkins.instance.getExtensionList('com.cloudbees.plugins.credentials.SystemCredentialsProvider')[0]?.getStore()
    def domain = new Domain(domainName, null, Collections.<DomainSpecification>emptyList())

    credentialsStore?.getCredentials(domain).each { cred ->

        if (cred instanceof UsernamePasswordCredentialsImpl) {
            writer.println("type=username_password")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            writer.println("username=${cred.username}")
            writer.println("password=${cred.password?.getPlainText()}")
            writer.println()

        } else if (cred instanceof BasicSSHUserPrivateKey) {
            writer.println("type=ssh_key")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            writer.println("username=${cred.username ?: ''}")
            // multiline private key — write verbatim
            writer.println("private_key=${cred.privateKeySource.getPrivateKey()}")
            writer.println("passphrase=${cred.passphrase ?: ''}")
            writer.println()

        } else if (cred instanceof AWSCredentialsImpl) {
            writer.println("type=aws")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            writer.println("access_key=${cred.accessKey}")
            writer.println("secret_key=${cred.secretKey?.getPlainText()}")
            writer.println()

        } else if (cred instanceof StringCredentials) {
            writer.println("type=secret_text")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            writer.println("secret=${cred.secret?.getPlainText()}")
            writer.println()

        } else if (cred instanceof FileCredentialsImpl) {
            writer.println("type=secret_file")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            // Cannot export file content directly, just mention filename
            writer.println("filename=${cred.fileName}")
            writer.println()

        } else {
            writer.println("type=unknown")
            writer.println("id=${cred.id}")
            writer.println("description=${cred.description ?: ''}")
            writer.println()
        }
    }
}

println "Credentials exported to: ${outputFile.absolutePath}"
 