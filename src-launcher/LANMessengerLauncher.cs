using System;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using System.Windows.Forms;

namespace LanMessengerLauncher
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
                    MessageBox.Show("Could not locate LAN Messenger project directory.\nPlease ensure the application folder is in place.", "LAN Messenger", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string clientJar = Path.Combine(projDir, @"lan-client\target\lan-client-1.0.0-executable.jar");
                if (!File.Exists(clientJar))
                {
                    clientJar = Path.Combine(projDir, "lan-client-1.0.0-executable.jar");
                }

                if (!File.Exists(clientJar))
                {
                    MessageBox.Show("Client executable JAR not found at:\n" + clientJar, "LAN Messenger Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string javaw = FindJavawExecutable();
                if (string.IsNullOrEmpty(javaw))
                {
                    MessageBox.Show("Java 17 runtime not found.\nPlease install Java 17 to run LAN Messenger.", "Java Not Found", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                    return;
                }

                // If port 5000 is not running, auto-start Server in background
                if (!IsPortListening(5000))
                {
                    string serverJar = Path.Combine(projDir, @"lan-server\target\lan-server-1.0.0-executable.jar");
                    if (!File.Exists(serverJar))
                    {
                        serverJar = Path.Combine(projDir, "lan-server-1.0.0-executable.jar");
                    }

                    if (File.Exists(serverJar))
                    {
                        ProcessStartInfo sPsi = new ProcessStartInfo();
                        sPsi.FileName = javaw;
                        sPsi.Arguments = string.Format("-jar \"{0}\"", serverJar);
                        sPsi.WorkingDirectory = projDir;
                        sPsi.UseShellExecute = true;
                        Process.Start(sPsi);

                        for (int i = 0; i < 15; i++)
                        {
                            System.Threading.Thread.Sleep(200);
                            if (IsPortListening(5000)) break;
                        }
                    }
                }

                // Launch Client GUI
                ProcessStartInfo cPsi = new ProcessStartInfo();
                cPsi.FileName = javaw;
                cPsi.Arguments = string.Format("-jar \"{0}\"", clientJar);
                cPsi.WorkingDirectory = projDir;
                cPsi.UseShellExecute = true;
                Process.Start(cPsi);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Failed to launch LAN Messenger:\n" + ex.Message, "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        static bool IsPortListening(int port)
        {
            try
            {
                using (TcpClient client = new TcpClient())
                {
                    IAsyncResult ar = client.BeginConnect("127.0.0.1", port, null, null);
                    bool ok = ar.AsyncWaitHandle.WaitOne(200);
                    if (!ok) return false;
                    client.EndConnect(ar);
                    return true;
                }
            }
            catch
            {
                return false;
            }
        }

        static string FindProjectDirectory(string startDir)
        {
            string dir = startDir;
            for (int i = 0; i < 4; i++)
            {
                if (Directory.Exists(Path.Combine(dir, "lan-client")) &&
                    File.Exists(Path.Combine(dir, @"lan-client\target\lan-client-1.0.0-executable.jar")))
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
