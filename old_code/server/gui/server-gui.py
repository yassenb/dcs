#!/usr/bin/env python

import wx
import socket
import httplib
import urllib
import ConfigParser
import os
import re
import threading
import time
import subprocess
import threading
import socket
import sys

class AddressValidator(wx.PyValidator):
    def __init__(self, allow_hostname):
        wx.PyValidator.__init__(self)
        self.__allow_hostname = allow_hostname

    def Clone(self):
        return AddressValidator(self.__allow_hostname)

    def Validate(self):
        text_ctrl = self.GetWindow()

        try:
            address = text_ctrl.GetValue()
            socket.inet_aton(address)
        except socket.error:
            if not (self.__allow_hostname and AddressValidator.__is_valid_hostname(address)):
                error_msg = "Please enter a valid IP adddress"
                if self.__allow_hostname:
                    error_msg += " or hostname"
                wx.MessageBox(error_msg, "Error", style=wx.OK | wx.ICON_ERROR)

                text_ctrl.SetBackgroundColour("pink")
                text_ctrl.SetFocus()
                text_ctrl.Refresh()
                return False

        text_ctrl.SetBackgroundColour(wx.SystemSettings_GetColour(wx.SYS_COLOUR_WINDOW))
        text_ctrl.Refresh()
        return True

    # copied from stackoverflow.com
    @staticmethod
    def __is_valid_hostname(hostname):
        if len(hostname) > 255:
            return False
        if hostname[-1:] == ".":
            hostname = hostname[:-1] # strip exactly one dot from the right, if present
        allowed = re.compile("(?!-)[A-Z\d-]{1,63}(?<!-)$", re.IGNORECASE)
        return all(allowed.match(x) for x in hostname.split("."))


class MainFrame(wx.Frame):
    def __init__(self):
        wx.Frame.__init__(self, None, title="DCS")

        self.Bind(wx.EVT_CLOSE, self.__on_close)

        self.__configuration = Configuration()
        self.__process_manager = ProcessManager()
        self.__state_manager = StateManager(self.__refresh_tbicon,
                                            self.__process_manager,
                                            self.__configuration.get_master_ip(),
                                            self.__configuration.get_bind_ip(),
                                            self.__configuration.get_registered())

        self.__tbicon = wx.TaskBarIcon()
        self.__tbicon.Bind(wx.EVT_TASKBAR_LEFT_DCLICK, self.__on_tbicon_click)
        self.__refresh_tbicon()

        self.Show(not self.__state_manager.get_registered())
        
        panel = wx.Panel(self)

        master_ip_static_text = wx.StaticText(panel, label="Master Node IP address or hostname:")
        self.__master_ip_text_ctrl = wx.TextCtrl(panel, size=(250, 25), validator=AddressValidator(True))
        bind_ip_static_text = wx.StaticText(panel, label="IP address to bind to (optional):")
        self.__bind_ip_text_ctrl = wx.TextCtrl(panel, size=(120, 25), validator=AddressValidator(False))

        self.__master_ip_text_ctrl.SetValue(self.__state_manager.get_master_ip() or "")
        self.__bind_ip_text_ctrl.SetValue(self.__state_manager.get_bind_ip() or "")

        register_button = wx.Button(panel, label="Register")
        register_button.SetDefault()
        register_button.Bind(wx.EVT_BUTTON, self.__on_register)

        # layout starts here

        sizer_ips = wx.FlexGridSizer(2, vgap=5, hgap=5)
        sizer_ips.Add(master_ip_static_text, flag=wx.ALIGN_CENTER_VERTICAL | wx.ALIGN_RIGHT)
        sizer_ips.Add(self.__master_ip_text_ctrl, flag=wx.ALIGN_CENTER_VERTICAL)

        sizer_ips.Add(bind_ip_static_text, flag=wx.ALIGN_CENTER_VERTICAL | wx.ALIGN_RIGHT)
        sizer_ips.Add(self.__bind_ip_text_ctrl, flag=wx.ALIGN_CENTER_VERTICAL)

        sizer_button = wx.BoxSizer(wx.HORIZONTAL)
        sizer_button.Add(register_button)

        sizer = wx.BoxSizer(wx.VERTICAL)
        sizer.Add(sizer_ips, flag=wx.ALIGN_LEFT | wx.ALIGN_CENTER_HORIZONTAL | wx.ALL, border=10)
        sizer.Add(sizer_button, flag=wx.ALIGN_CENTER_HORIZONTAL | wx.ALL, border=10)

        panel.SetSizer(sizer)
        sizer.Fit(self)

    def __refresh_tbicon(self):
        self.__tbicon.SetIcon(wx.Icon(self.__get_icon_name(), wx.BITMAP_TYPE_PNG), self.__get_tooltip())

    def __on_register(self, event):
        if not self.__master_ip_text_ctrl.GetValidator().Validate() or \
           (self.__bind_ip_text_ctrl.GetValue() != "" and not self.__bind_ip_text_ctrl.GetValidator().Validate()):
               return
        try:
            self.__state_manager.register(self.__master_ip_text_ctrl.GetValue(), self.__bind_ip_text_ctrl.GetValue())
            wx.MessageBox("Successfully registered with Master Node")
        except:
            wx.MessageBox("Problem registering with given Master Node", "Error", style=wx.OK | wx.ICON_ERROR)

        self.__refresh_tbicon()

    def __get_icon_name(self):
        if self.__state_manager.get_registered() and self.__state_manager.get_online():
            name = "network-idle"
        else:
            name = "network-offline"

        if "wxMSW" in wx.PlatformInfo:
            suffix = "-16x16"
        elif "wxGTK" in wx.PlatformInfo:
            suffix = "-22x22"

        return Configuration.get_base_path() + "/" + "icons/" + name + suffix + ".png"

    def __get_tooltip(self):
        if self.__state_manager.get_registered():
            tt = "registered"
            tt += " and "
            if self.__state_manager.get_online():
                tt += "online"
            else:
                tt += "offline"
        else:
            tt = "not registered"

        return tt

    def __on_close(self, event):
        self.__configuration.save(self.__state_manager.get_master_ip(),
                                  self.__state_manager.get_bind_ip(),
                                  self.__state_manager.get_registered())
        self.__process_manager.end()
        self.__tbicon.RemoveIcon()
        event.Skip()

    def __on_tbicon_click(self, event):
        self.Show(not self.IsShown())


class ProcessManager:
    def __init__(self):
        self.__rmi_registry_process, self.__service_process = None, None

    def spawn_processes(self, ip):
        if not self.__rmi_registry_process or self.__rmi_registry_process.poll() != None:
            # TODO fix for win users if they don't have it in path
            # and hard-coded port
            self.__rmi_registry_process = subprocess.Popen(["rmiregistry", "55556"])

        if not self.__service_process or self.__service_process.poll() != None:
            curdir = Configuration.get_base_path()
            self.__service_process = subprocess.Popen(
                ["java",
                 "-Djava.rmi.server.hostname={0}".format(ip),
                 "-Djava.rmi.server.codebase=file:{0}/lib/executor.jar".format(os.path.abspath(curdir)),
                 "-Djava.security.policy=server.policy",
                 "-jar",
                 "lib/service.jar"],
                cwd=curdir)

    def end(self):
        if self.__rmi_registry_process:
            self.__rmi_registry_process.kill()
        if self.__service_process:
            self.__service_process.kill()


class StateManager:
    # TODO fix multi-threaded access
    class __MonitorThread(threading.Thread):
        def __init__(self, monitoree):
            threading.Thread.__init__(self)
            self.daemon = True
            self.__monitoree = monitoree

        def run(self):
            while True:
                if self.__monitoree.get_registered() and self.__monitoree.check_status():
                    self.__monitoree.spawn_processes()
                # TODO change
                time.sleep(5)

    def __init__(self, status_changed_callback, process_manager, master_ip, bind_ip, registered):
        self.__status_changed_callback = status_changed_callback
        self.__process_manager = process_manager
        self.__ip = None
        self.__online = False
        self.__master_ip = master_ip
        self.__bind_ip = bind_ip
        self.__registered = registered
        self.__MonitorThread(self).start()

    def get_master_ip(self):
        return self.__master_ip

    def get_bind_ip(self):
        return self.__bind_ip

    def get_registered(self):
        return self.__registered

    def get_online(self):
        return self.__online

    def check_status(self):
        try:
            status = self.__act_on_client({"action": "check"}).strip()
            self.__registered = True
            self.__ip = self.__act_on_client({"action": "getmyip"}).strip()
            self.__online = True
            if status != "OK":
                self.__registered = False
        except:
            self.__registered = False
            self.__online = False

        wx.CallAfter(self.__status_changed_callback)
        return self.__registered and self.__online

    def spawn_processes(self):
        self.__process_manager.spawn_processes(self.__ip)

    def register(self, master_ip, bind_ip):
        # the processes will be respawned on the next status check
        self.__process_manager.end()
        self.__registered = False

        self.__master_ip = master_ip
        self.__bind_ip = bind_ip

        s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            # TODO fix hard-coded port
            try:
                s.bind((self.__act_on_client({"action": "getmyip"}).strip(), 55555))
            except Exception as e:
                print(e)
        finally:
            s.close()

        self.__act_on_client({"action": "register"})
        self.__registered = True

    def __act_on_client(self, params):
        if self.__bind_ip:
            conn = httplib.HTTPConnection(self.__master_ip, source_address=self.__bind_ip)
        else:
            conn = httplib.HTTPConnection(self.__master_ip)

        params = urllib.urlencode(params)
        try:
            conn.request("GET", "/cgi-bin/action.py?" + params)
            response = conn.getresponse()
            if response.status == 200:
                return response.read()
            else:
                raise IOError("error from server")
        finally:
            conn.close()


class Configuration:
    __CONFIG_FILE_NAME = "dcs.cfg"
    __IPS_SECTION = "IPs"
    __STATUS_SECTION = "status"
    __MASTER_IP = "Master Node IP"
    __BIND_IP = "IP to bind to"
    __REGISTERED = "Successfully registered"

    def __init__(self):
        self.__read()

    def __read(self):
        self.__master_ip, self.__bind_ip, self.__registered = None, None, None
        file = self.get_base_path() + "/" + self.__CONFIG_FILE_NAME
        if os.path.isfile(file):
            config = ConfigParser.SafeConfigParser()
            try:
                config.read(file)
                self.__master_ip = config.get(self.__IPS_SECTION, self.__MASTER_IP)
                self.__bind_ip = config.get(self.__IPS_SECTION, self.__BIND_IP)
                self.__registered = config.getboolean(self.__STATUS_SECTION, self.__REGISTERED)
            except ConfigParser.Error:
                pass

    def get_master_ip(self):
        return self.__master_ip

    def get_bind_ip(self):
        return self.__bind_ip

    def get_registered(self):
        return self.__registered

    def save(self, master_ip, bind_ip, registered):
        config = ConfigParser.SafeConfigParser()
        config.add_section(self.__IPS_SECTION)
        config.set(self.__IPS_SECTION, self.__MASTER_IP, master_ip or self.__master_ip or "")
        config.set(self.__IPS_SECTION, self.__BIND_IP, bind_ip or self.__bind_ip or "")
        config.add_section(self.__STATUS_SECTION)
        config.set(self.__STATUS_SECTION, self.__REGISTERED, str(registered or "False"))

        with open(self.get_base_path() + "/" + self.__CONFIG_FILE_NAME, "wb") as configfile:
            config.write(configfile)

    @classmethod
    def get_base_path(cls):
        return os.path.dirname(sys.argv[0])


app = wx.App()
MainFrame()
if __name__ == "__main__":
    app.MainLoop()
