using System;
using System.Diagnostics;
using System.IO;
using System.Net.Sockets;
using System.Windows.Forms;

namespace LanDemoLauncher
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
                    MessageBox.Show("Could not locate LAN Messenger project directory.", "LAN Demo", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                string serverJar = Path.Combine(projDir, @"lan-server\target\lan-server-1.0.0-executable.jar");
                string clientJar = Path.Combine(projDir, @"lan-client\target\lan-client-1.0.0-executable.jar");

                string javaw = FindJavawExecutable();

                // 1. Launch Server if not already running
                if (!IsPortListening(5000) && File.Exists(serverJar))
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

                // 2. Launch Client 1 (Alice)
                ProcessStartInfo c1Psi = new ProcessStartInfo();
                c1Psi.FileName = javaw;
                c1Psi.Arguments = string.Format("-jar \"{0}\"", clientJar);
                c1Psi.WorkingDirectory = projDir;
                c1Psi.UseShellExecute = true;
                Process.Start(c1Psi);

                System.Threading.Thread.Sleep(500);

                // 3. Launch Client 2 (Bob)
                ProcessStartInfo c2Psi = new ProcessStartInfo();
                c2Psi.FileName = javaw;
                c2Psi.Arguments = string.Format("-jar \"{0}\"", clientJar);
                c2Psi.WorkingDirectory = projDir;
                c2Psi.UseShellExecute = true;
                Process.Start(c2Psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show("Failed to launch LAN Demo:\n" + ex.Message, "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
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
