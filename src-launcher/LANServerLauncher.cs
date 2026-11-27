using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;

namespace LanServerLauncher
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                string baseDir = AppDomain.CurrentDomain.BaseDirectory;
                string projDir = FindProjectDirectory(baseDir);
                if (string.IsNullOrEmpty(projDir))
                {
                    MessageBox.Show("Could not locate LAN Messenger project directory.", "LAN Server", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string serverJar = Path.Combine(projDir, @"lan-server\target\lan-server-1.0.0-executable.jar");
                if (!File.Exists(serverJar))
                {
                    serverJar = Path.Combine(projDir, "lan-server-1.0.0-executable.jar");
                }

                if (!File.Exists(serverJar))
                {
                    MessageBox.Show("Server executable JAR not found at:\n" + serverJar, "LAN Server Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string javaw = FindJavawExecutable();
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaw;
                psi.Arguments = string.Format("-jar \"{0}\"", serverJar);
                psi.WorkingDirectory = projDir;
                psi.UseShellExecute = true;
                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Failed to launch LAN Server:\n" + ex.Message, "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        static string FindProjectDirectory(string startDir)
        {
            string dir = startDir;
            for (int i = 0; i < 4; i++)
            {
                if (Directory.Exists(Path.Combine(dir, "lan-server")) &&
                    File.Exists(Path.Combine(dir, @"lan-server\target\lan-server-1.0.0-executable.jar")))
                {
                    return dir;
                }
                DirectoryInfo parent = Directory.GetParent(dir);
                if (parent == null) break;
                dir = parent.FullName;
            }

            string defaultDir = @"c:\Users\vidhi\Downloads\z Java Bv project 1\z Java Bv project";
            if (Directory.Exists(defaultDir)) return defaultDir;
            return null;
        }

        static string FindJavawExecutable()
        {
            string localApp = Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData);
            string[] candidates = new string[]
            {
                @"C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\javaw.exe",
                Path.Combine(localApp, @"Programs\Eclipse Adoptium\jdk-17\bin\javaw.exe"),
                @"C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe",
                Path.Combine(localApp, @"Programs\Eclipse Adoptium\jdk-17\bin\java.exe")
            };

            foreach (string p in candidates)
            {
                if (File.Exists(p)) return p;
            }

            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome))
            {
                string p = Path.Combine(javaHome, @"bin\javaw.exe");
                if (File.Exists(p)) return p;
                p = Path.Combine(javaHome, @"bin\java.exe");
                if (File.Exists(p)) return p;
            }

            return "javaw.exe";
        }
    }
}
