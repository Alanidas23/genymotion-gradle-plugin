/*
 * Copyright (C) 2015 Genymobile
 *
 * This file is part of GenymotionGradlePlugin.
 *
 * GenymotionGradlePlugin is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GenymotionGradlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 */

package test.groovy.com.genymotion

import main.groovy.com.genymotion.model.GenymotionConfig
import main.groovy.com.genymotion.model.GenymotionVDLaunch
import main.groovy.com.genymotion.tools.GMTool
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class TestTools {

    static def DEVICES = [
            "Nexus7-junit":"Google Nexus 7 - 4.1.1 - API 16 - 800x1280",
            "Nexus10-junit":"Google Nexus 10 - 4.4.4 - API 19 - 2560x1600",
            "Nexus4-junit":"Google Nexus 4 - 4.3 - API 18 - 768x1280"
    ]

    static def init(){

        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'genymotion'

        project.genymotion.config.genymotionPath = getDefaultConfig().genymotionPath
        project.genymotion.config.verbose = true
        //we set the config inside the GenymotionTool
        GMTool.GENYMOTION_CONFIG = project.genymotion.config

        GMTool.getConfig(true)

        project
    }

    static void deleteAllDevices() {
        DEVICES.each() { key, value ->
            GMTool.deleteDevice(key)
        }
    }

    static void createAllDevices() {
        DEVICES.each() { key, value ->
            GMTool.createDevice(value, key)
        }
    }

    static String createADevice() {

        Random rand = new Random()
        int index = rand.nextInt(DEVICES.size())

        String[] keys = DEVICES.keySet() as String[]
        String name = keys[index]
        GMTool.createDevice(DEVICES[name], name)

        name
    }

    static def declareADetailedDevice(Project project, boolean stop=true) {
        String vdName = GenymotionVDLaunch.getRandomName("-junit")
        String densityName = "mdpi"
        int heightInt = 480
        int widthInt = 320
        int ramInt = 2048
        int nbCpuInt = 1
        boolean delete = true

        project.genymotion.devices {
            "$vdName" {
                template "Google Nexus 7 - 4.1.1 - API 16 - 800x1280"
                density densityName
                width widthInt
                height heightInt
                virtualKeyboard false
                navbarVisible false
                nbCpu nbCpuInt
                ram ramInt
                deleteWhenFinish delete
                stopWhenFinish stop
            }
        }
        [vdName, densityName, widthInt, heightInt, nbCpuInt, ramInt, delete]
    }


    static void cleanAfterTests(){

        println "Cleaning after tests"

        GMTool.getConfig(true)

        try{
            def devices = GMTool.getAllDevices(false, false, false)
            def pattern = ~/^.+?\-junit$/
            println devices

            devices.each(){
                if(pattern.matcher(it.name).matches()){
                    println "Removing $it.name"
                    if(it.isRunning())
                        GMTool.stopDevice(it.name, true)
                    GMTool.deleteDevice(it.name, true)
                }
            }
        } catch (Exception e){
            println e
        }

        //Delete temp folder
        new File("temp").deleteDir()
    }

    static void recreatePulledDirectory() {
        File tempDir = new File("temp/pulled")
        if (tempDir.exists()) {
            if (tempDir.isDirectory())
                tempDir.deleteDir()
            else
                tempDir.delete()
        }
        tempDir.mkdirs()
    }

    static GenymotionConfig getDefaultConfig(String path = "res/test/default.properties"){
        // We get the APK signing properties from a file
        GenymotionConfig config = new GenymotionConfig()
        config.fromFile = path

        if(config.applyConfigFromFile(null))
            return config

        return null
    }

    static setDefaultUser(registerLicense = false){
        GenymotionConfig config = getDefaultConfig()

        if(config.username && config.password){
            GMTool.setConfig(config, true)

            if(config.license && registerLicense)
                GMTool.setLicense(config.license, null, null, true)
        }
    }
}
