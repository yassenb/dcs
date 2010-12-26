#!/usr/bin/env python
# coding=utf-8

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

        self.set_status("offline")
        self.__base_path = os.path.dirname(__file__) + "/"
        self.__master_ip, self.__bind_ip, self.registered = self.__read_config()

        self.Bind(wx.EVT_CLOSE, self.__on_close)

        self.__tbicon = wx.TaskBarIcon()
        self.__tbicon.Bind(wx.EVT_TASKBAR_LEFT_DCLICK, self.__on_tbicon_click)
        self.refresh_tbicon()

        self.Show(not self.registered)
        
        panel = wx.Panel(self)

        master_ip_static_text = wx.StaticText(panel, label="Master Node IP address or hostname:")
        self.__master_ip_text_ctrl = wx.TextCtrl(panel, size=(250, 25), validator=AddressValidator(True))
        bind_ip_static_text = wx.StaticText(panel, label="IP address to bind to (optional):")
        self.__bind_ip_text_ctrl = wx.TextCtrl(panel, size=(120, 25), validator=AddressValidator(False))

        self.__master_ip_text_ctrl.SetValue(self.__master_ip or "")
        self.__bind_ip_text_ctrl.SetValue(self.__bind_ip or "")

        self.__monitor = MonitorThread(self)
        self.__monitor.start()

        register_button = wx.Button(panel, label="Register")
        register_button.SetDefault()
        register_button.Bind(wx.EVT_BUTTON, self.__on_register)
        
        self.__rmi_registry_process = subprocess.Popen(["rmiregistry"])

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

    def refresh_tbicon(self):
        self.__tbicon.SetIcon(wx.Icon(self.__get_icon_name(), wx.BITMAP_TYPE_PNG), self.__get_tooltip())

    def act_on_client(self, params):
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
                return False
        except:
            return False
        finally:
            conn.close()

    def set_status(self, status):
        self.__online = False
        if status == "online":
            self.__online = True
        elif status == "offline":
            pass

    def __on_register(self, event):
        self.registered = False
        if not self.__master_ip_text_ctrl.GetValidator().Validate() or \
           (self.__bind_ip_text_ctrl.GetValue() != "" and not self.__bind_ip_text_ctrl.GetValidator().Validate()):
               return
        try:
            self.act_on_client({"action": "register"})
            self.registered = True
            self.__master_ip = self.__master_ip_text_ctrl.GetValue()
            self.__bind_ip = self.__bind_ip_text_ctrl.GetValue()
            wx.MessageBox("Successfully registered with Master Node")
        except:
            wx.MessageBox("Problem registering with given Master Node", "Error", style=wx.OK | wx.ICON_ERROR)

    def __get_icon_name(self):
        if self.__online and self.registered:
            name = "network-idle"
        else:
            name = "network-offline"

        if "wxMSW" in wx.PlatformInfo:
            suffix = "-16x16"
        elif "wxGTK" in wx.PlatformInfo:
            suffix = "-22x22"

        return self.__base_path + "icons/" + name + suffix + ".png"

    def __get_tooltip(self):
        if self.registered:
            tt = "registered"
            tt += " and "
            if self.__online:
                tt += "online"
            else:
                tt += "offline"
        else:
            tt = "not registered"

        return tt

    __CONFIG_FILE_NAME = "dcs.cfg"
    __IPS_SECTION = "IPs"
    __MASTER_IP = "Master Node IP"
    __BIND_IP = "IP to bind to"
    __REGISTERED = "Successfully registered"
    def __read_config(self):
        config = ConfigParser.SafeConfigParser()
        try:
            config.read(self.__base_path + self.__CONFIG_FILE_NAME)
            master_ip = config.get(self.__IPS_SECTION, self.__MASTER_IP)
            bind_ip = config.get(self.__IPS_SECTION, self.__BIND_IP)
            registered = config.getboolean(self.__IPS_SECTION, self.__REGISTERED)
            return (master_ip, bind_ip, registered)
        except ConfigParser.Error:
            return (None, None, None)

    def __on_close(self, event):
        self.__save_config()
        self.__rmi_registry_process.kill()
        event.Skip()

    def __save_config(self):
        config = ConfigParser.SafeConfigParser()
        config.add_section(self.__IPS_SECTION)
        config.set(self.__IPS_SECTION, self.__MASTER_IP, self.__master_ip)
        config.set(self.__IPS_SECTION, self.__BIND_IP, self.__bind_ip)
        config.set(self.__IPS_SECTION, self.__REGISTERED, str(self.registered))

        with open(self.__base_path + self.__CONFIG_FILE_NAME, "wb") as configfile:
            config.write(configfile)

    def __on_tbicon_click(self, event):
        self.Show(not self.IsShown())

# TODO fix monitoree.registered multi-threaded access
class MonitorThread(threading.Thread):
    def __init__(self, monitoree):
        threading.Thread.__init__(self)
        self.daemon = True
        self.__monitoree = monitoree

    def run(self):
        while True:
            offline = False
            if self.__monitoree.registered:
                try:
                    if self.__monitoree.act_on_client({"action": "check"}).strip() != "OK":
                        self.__monitoree.registered = False
                except:
                    offline = True

            if offline or not self.__monitoree.registered:
                self.__monitoree.set_status("offline")
            else:
                self.__monitoree.set_status("online")

            self.__monitoree.refresh_tbicon()

            time.sleep(30)


app = wx.App()
MainFrame()
if __name__ == "__main__":
    app.MainLoop()
